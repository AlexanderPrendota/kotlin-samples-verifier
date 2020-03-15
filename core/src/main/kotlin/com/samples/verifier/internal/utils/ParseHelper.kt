package com.samples.verifier.internal.utils

import com.github.rjeschke.txtmark.BlockEmitter
import com.github.rjeschke.txtmark.Configuration
import com.github.rjeschke.txtmark.Processor
import com.samples.verifier.SamplesVerifier
import org.slf4j.LoggerFactory
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.lang.StringBuilder
import java.nio.file.Paths

private val logger = LoggerFactory.getLogger(SamplesVerifier::class.java)

internal fun processFile(
    file: File,
    targetDir: String,
    sourceDir: String,
    flags: List<String>,
    execute: Boolean = false,
    requestHelper: RequestHelper? = null
) {
    val txtmarkConfiguration = Configuration.builder()
        .forceExtentedProfile()
        .setCodeBlockEmitter(
            CodeBlockEmitter(
                targetDir = targetDir,
                flags = flags,
                filename = file.nameWithoutExtension,
                path = file.toString().substringAfter(sourceDir).substringBeforeLast('.'),
                execute = execute,
                requestHelper = requestHelper
            )
        )
        .build()
    try {
        Processor.process(file, txtmarkConfiguration)
    } catch (e: Exception) {
        logger.error("${e.message}\n")
    }
}

private class CodeBlockEmitter(
    targetDir: String,
    val flags: List<String>,
    val filename: String,
    val path: String,
    val execute: Boolean,
    val requestHelper: RequestHelper? = null
) :
    BlockEmitter {
    private var counter = 1
    private val dir = Paths.get("${targetDir}/${path}").toAbsolutePath().toString()

    override fun emitBlock(out: StringBuilder, lines: MutableList<String>?, meta: String?) {
        if (meta in flags && lines != null) {
            File(dir).mkdirs()
            val ktFilename = "$dir/${filename}_$counter.kt"

            if (execute && requestHelper != null) {
                requestHelper.compileCodeRequest(
                    project = Project(
                        "",
                        listOf(KotlinFile(ktFilename, lines.joinToString("\n")))
                    ),
                    filename = ktFilename
                )
            }

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