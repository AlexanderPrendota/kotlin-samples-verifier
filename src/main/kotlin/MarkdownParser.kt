import com.github.rjeschke.txtmark.BlockEmitter
import com.github.rjeschke.txtmark.Configuration
import com.github.rjeschke.txtmark.Processor
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.URIish
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.BufferedWriter
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.StringBuilder

class MarkdownParser(private val config: Config) {
    private val logger: Logger by lazy { LoggerFactory.getLogger(javaClass) }

    constructor(sourceDir: String, block: Config.() -> Unit) : this(setConfiguration(sourceDir, block))

    constructor(repositoryURL: URIish, block: Config.() -> Unit) : this(setConfiguration(repositoryURL, block))

    fun processGitRepository() {
        try {
            cloneRep()
        } catch (e: Exception) {
            if (File(config.sourceDir).isDirectory) {
                FileUtils.deleteDirectory(File(config.sourceDir))
            }
            logger.error("${e.message}\n")
            return
        }
        processDirectory()
    }

    fun processDirectory() {
        val sourceDirectory = File(config.sourceDir)
        Files.walk(sourceDirectory.toPath()).use {
            it.forEach { path: Path ->
                val file = path.toFile()
                if (file.extension == "md") {
                    processFile(file)
                }
            }
        }
    }

    private fun cloneRep() {
        if (config.repositoryURL == null) {
            throw Exception("No repository URL provided")
        }
        val dir = File(Paths.get(config.sourceDir).toAbsolutePath().toString())
        dir.mkdirs()
        val git = Git.cloneRepository()
            .setURI(config.repositoryURL.toString())
            .setDirectory(dir)
            .call()
        git.close()
    }

    private fun processFile(file: File) {
        val txtmarkConfiguration = Configuration.builder()
            .forceExtentedProfile()
            .setCodeBlockEmitter(
                CodeBlockEmitter(
                    config,
                    filename = file.nameWithoutExtension,
                    path = file.toString().substringAfter(config.sourceDir).substringBeforeLast('.')
                )
            )
            .build()
        try {
            Processor.process(file, txtmarkConfiguration)
        } catch (e: Exception) {
            logger.error("${e.message}\n")
            logger.error("Unable to parse $file\n")
        }
    }
}

class CodeBlockEmitter(val config: Config, val filename: String, val path: String) : BlockEmitter {
    private var counter = 1
    private val dir = Paths.get("${config.targetDir}/${path}").toAbsolutePath().toString()

    override fun emitBlock(out: StringBuilder, lines: MutableList<String>?, meta: String?) {
        if (meta in config.flags && lines != null) {
            File(dir).mkdirs()
            val ktFilename = "$dir/${filename}_$counter.kt"
            val fileWriter = FileWriter(ktFilename)
            val bufferedWriter = BufferedWriter(fileWriter)
            for (line in lines) {
                bufferedWriter.write(line)
                bufferedWriter.newLine()
            }
            bufferedWriter.close()
            counter++
        }
    }
}