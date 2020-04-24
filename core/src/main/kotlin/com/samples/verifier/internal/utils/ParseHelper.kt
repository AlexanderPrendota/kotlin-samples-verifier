package com.samples.verifier.internal.utils

import com.github.rjeschke.txtmark.BlockEmitter
import com.github.rjeschke.txtmark.Configuration
import com.github.rjeschke.txtmark.Processor
import com.samples.verifier.Code
import org.jsoup.Jsoup
import com.samples.verifier.FileType
import org.slf4j.LoggerFactory
import java.io.File
import java.lang.StringBuilder

private val logger = LoggerFactory.getLogger("Samples Verifier")

internal var TEST_PARSE = false

internal fun processFile(file: File, type: FileType, flags: List<String>): List<Code> = when (type) {
    FileType.MD -> processMarkdownFile(file, flags)
    FileType.HTML -> processHTMLFile(file, flags)
}

private fun processHTMLFile(file: File, flags: List<String>): List<Code> {
    val document = Jsoup.parse(file, null)
    val snippets = mutableListOf<Code>()
    for (elem in document.allElements) {
        for (flag in flags) {
            if (elem.hasClass(flag)) {
                val code = elem.wholeText().trimIndent()
                snippets.add(code)
                break
            }
        }
    }
    return snippets
}

private fun processMarkdownFile(file: File, flags: List<String>): List<Code> {
    if (TEST_PARSE) {
        val snippets = mutableListOf<Code>()
        val text = file.readText().split("```")
        for ((i,t) in text.withIndex()) {
            if (i % 2 == 1) {
                val tmp = t.split("\n", limit = 2)
                if (tmp.size < 2) continue
                val (meta, code) = tmp
                if (meta.trim() in flags) {
                    snippets.add(if (code.last() == '\n') code.dropLast(1) else code)
                }
            }
        }
        return snippets
    }

    val snippets = mutableListOf<Code>()
    val txtmarkConfiguration = Configuration.builder()
        .forceExtentedProfile()
        .setCodeBlockEmitter(
            CodeBlockEmitter(
                flags = flags,
                snippets = snippets
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
    return snippets
}

private class CodeBlockEmitter(val flags: List<String>, val snippets: MutableList<Code>) : BlockEmitter {
    override fun emitBlock(out: StringBuilder, lines: MutableList<String>?, meta: String?) {
        if (meta in flags && lines != null) {
            val code = lines.joinToString("\n")
            snippets.add(code)
        }
    }
}