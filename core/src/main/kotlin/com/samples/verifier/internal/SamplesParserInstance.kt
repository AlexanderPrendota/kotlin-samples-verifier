package com.samples.verifier.internal

import com.github.rjeschke.txtmark.BlockEmitter
import com.github.rjeschke.txtmark.Configuration
import com.github.rjeschke.txtmark.Processor
import com.samples.verifier.SamplesParser
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

class SamplesParserInstance(
    override var sourceDir: String,
    override var flags: List<String>,
    override var targetDir: String = "${sourceDir}_snippets"
) : SamplesParser {
    override var repositoryURL: URIish? = null
    private val logger: Logger by lazy { LoggerFactory.getLogger(javaClass) }

    constructor(repositoryURL: URIish, flags: List<String>, targetDir: String = "${repositoryURL.humanishName}_snippets") : this(
        sourceDir = repositoryURL.humanishName,
        flags = flags,
        targetDir = targetDir
    ) {
        this.repositoryURL = repositoryURL
    }

    override fun processGitRepository() {
        try {
            cloneRep()
        } catch (e: Exception) {
            if (File(sourceDir).isDirectory) {
                FileUtils.deleteDirectory(File(sourceDir))
            }
            logger.error("${e.message}\n")
            return
        }
        processDirectory()
    }

    override fun processDirectory() {
        val sourceDirectory = File(sourceDir)
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
        if (repositoryURL == null) {
            throw Exception("No repository URL provided")
        }
        val dir = File(Paths.get(sourceDir).toAbsolutePath().toString())
        dir.mkdirs()
        val git = Git.cloneRepository()
            .setURI(repositoryURL.toString())
            .setDirectory(dir)
            .call()
        git.close()
    }

    private fun processFile(file: File) {
        val txtmarkConfiguration = Configuration.builder()
            .forceExtentedProfile()
            .setCodeBlockEmitter(
                CodeBlockEmitter(
                    targetDir = targetDir,
                    flags = flags,
                    filename = file.nameWithoutExtension,
                    path = file.toString().substringAfter(sourceDir).substringBeforeLast('.')
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

class CodeBlockEmitter(targetDir: String, val flags: List<String>, val filename: String, val path: String) :
    BlockEmitter {
    private var counter = 1
    private val dir = Paths.get("${targetDir}/${path}").toAbsolutePath().toString()

    override fun emitBlock(out: StringBuilder, lines: MutableList<String>?, meta: String?) {
        if (meta in flags && lines != null) {
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