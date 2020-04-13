package com.samples.verifier.internal.utils

import com.github.rjeschke.txtmark.BlockEmitter
import com.github.rjeschke.txtmark.Configuration
import com.github.rjeschke.txtmark.Processor
import org.jsoup.Jsoup
import com.samples.verifier.FileType
import com.samples.verifier.model.ExecutionResult
import com.samples.verifier.model.KotlinFile
import org.slf4j.LoggerFactory
import java.io.File
import java.lang.StringBuilder

private val logger = LoggerFactory.getLogger("Samples Verifier")

internal fun processFile(
    file: File,
    type: FileType,
    flags: List<String>,
    executionHelper: ExecutionHelper,
    processResult: (ExecutionResult, KotlinFile) -> Unit
) {
    when (type) {
        FileType.MD -> {
            processMarkdownFile(file, flags, executionHelper, processResult)
        }
        FileType.HTML -> {
            processHTMLFile(file, flags, executionHelper, processResult)
        }
    }
}

private fun processHTMLFile(
    file: File,
    flags: List<String>,
    executionHelper: ExecutionHelper,
    processResult: (ExecutionResult, KotlinFile) -> Unit
) {
    var counter = 1
    val document = Jsoup.parse(file, null)
    for (elem in document.allElements) {
        for (flag in flags) {
            if (elem.hasClass(flag)) {
                val ktFilename = "${file.nameWithoutExtension}_$counter.kt"
                executionHelper.executeCode(
                    KotlinFile(
                        ktFilename,
                        elem.wholeText().trimIndent()
                    ),
                    processResult
                )
                counter++
                break
            }
        }
    }
}

private fun processMarkdownFile(
    file: File,
    flags: List<String>,
    executionHelper: ExecutionHelper,
    processResult: (ExecutionResult, KotlinFile) -> Unit
) {
    val txtmarkConfiguration = Configuration.builder()
        .forceExtentedProfile()
        .setCodeBlockEmitter(
            CodeBlockEmitter(
                flags = flags,
                filename = file.nameWithoutExtension,
                executionHelper = executionHelper,
                processResult = processResult
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
    val executionHelper: ExecutionHelper,
    val processResult: (ExecutionResult, KotlinFile) -> Unit
) :
    BlockEmitter {
    private var counter = 1

    override fun emitBlock(out: StringBuilder, lines: MutableList<String>?, meta: String?) {
        if (meta in flags && lines != null) {
            val ktFilename = "${filename}_$counter.kt"
            executionHelper.executeCode(
                KotlinFile(
                    ktFilename,
                    lines.joinToString("\n")
                ),
                processResult
            )
            counter++
        }
    }
}