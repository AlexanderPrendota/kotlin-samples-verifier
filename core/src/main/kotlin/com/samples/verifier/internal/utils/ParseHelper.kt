package com.samples.verifier.internal.utils

import com.github.rjeschke.txtmark.BlockEmitter
import com.github.rjeschke.txtmark.Configuration
import com.github.rjeschke.txtmark.Processor
import com.samples.verifier.FileType
import com.samples.verifier.model.KotlinFile
import org.slf4j.LoggerFactory
import java.io.File
import java.lang.StringBuilder

private val logger = LoggerFactory.getLogger("Samples Verifier")

internal fun processFile(
    file: File,
    type: FileType,
    flags: List<String>,
    requestHelper: RequestHelper
) {
    when (type) {
        FileType.MD -> {
            processMarkdownFile(file, flags, requestHelper)
        }
        FileType.HTML -> {
            processHTMLFile(file, flags, requestHelper)
        }
    }
}

private fun processHTMLFile(
    file: File,
    flags: List<String>,
    requestHelper: RequestHelper
) {
    TODO("Not yet implemented")
}

private fun processMarkdownFile(
    file: File,
    flags: List<String>,
    requestHelper: RequestHelper
) {
    val txtmarkConfiguration = Configuration.builder()
        .forceExtentedProfile()
        .setCodeBlockEmitter(
            CodeBlockEmitter(
                flags = flags,
                filename = file.nameWithoutExtension,
                requestHelper = requestHelper
            )
        )
        .build()
    try {
        Processor.process(file, txtmarkConfiguration)
    } catch (e: Exception) {
        if (logger.isInfoEnabled) {
            logger.error("${e.message}\n")
        } else logger.error("${e.message} while processing ${file}\n")
    }
}

private class CodeBlockEmitter(
    val flags: List<String>,
    val filename: String,
    val requestHelper: RequestHelper
) :
    BlockEmitter {
    private var counter = 1

    override fun emitBlock(out: StringBuilder, lines: MutableList<String>?, meta: String?) {
        if (meta in flags && lines != null) {
            val ktFilename = "${filename}_$counter.kt"
            requestHelper.executeCode(
                KotlinFile(
                    ktFilename,
                    lines.joinToString("\n")
                )
            )
            counter++
        }
    }
}