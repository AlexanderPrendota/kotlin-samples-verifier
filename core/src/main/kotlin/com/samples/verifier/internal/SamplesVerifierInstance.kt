package com.samples.verifier.internal

import com.samples.verifier.SamplesVerifier
import com.samples.verifier.internal.utils.processFile
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.URIish
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class SamplesVerifierInstance(override var config: Config) : SamplesVerifier {
    private val logger: Logger by lazy { LoggerFactory.getLogger(javaClass) }

    override fun run() {
        processFiles()
        TODO("files execution")
    }

    override fun run(repositoryURL: URIish) {
        config.repositoryURL = repositoryURL
        config.sourceDir = repositoryURL.humanishName
        try {
            cloneRep()
        } catch (e: Exception) {
            if (File(config.sourceDir).isDirectory) {
                FileUtils.deleteDirectory(File(config.sourceDir))
            }
            logger.error("${e.message}\n")
            return
        }
        run()
    }

    override fun run(sourceDir: String) {
        config.sourceDir = sourceDir
        run()
    }

    private fun processFiles() {
        val sourceDirectory = File(config.sourceDir)
        Files.walk(sourceDirectory.toPath()).use {
            it.forEach { path: Path ->
                val file = path.toFile()
                TODO("files execution")
                if (file.extension == "md") {
                    processFile(file, flags = config.flags,
                        sourceDir = config.sourceDir,
                        targetDir = config.targetDir)
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
}