package com.samples.verifier.internal.utils

import com.samples.verifier.Code
import com.samples.verifier.FileType
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.data.MutableDataSet
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.File
import java.util.*

internal fun processFile(
    file: File,
    type: FileType,
    flags: List<String>,
    ignoreAttributes: List<String> = emptyList()
): List<Code> = when (type) {
    FileType.MD -> processMarkdownFile(file, flags.map { "language-$it" }, ignoreAttributes)
    FileType.HTML -> processHTMLFile(file, flags, ignoreAttributes)
}

private fun processHTMLFile(
    file: File,
    flags: List<String>,
    ignoreAttributes: List<String>
): List<Code> {
    return processHTMLText(file.readText(), flags, ignoreAttributes)
}

private fun processMarkdownFile(
    file: File,
    flags: List<String>,
    ignoreAttributes: List<String>
): List<Code> {
    val options = MutableDataSet()
    val parser = Parser.builder(options).build()
    val node: Node = parser.parse(file.readText())
    val render = HtmlRenderer.builder(options).build()
    val htmlText = render.render(node)
    return processHTMLText(htmlText, flags, ignoreAttributes)
}

private fun processHTMLText(
    text: String,
    flags: List<String>,
    ignoreAttributes: List<String>
): List<Code> {
    val document = Jsoup.parse(text)
    val snippets = mutableListOf<Code>()
    val queue = LinkedList<Element>()
    queue.addFirst(document.body())
    while (queue.isNotEmpty()) {
        val elem = queue.remove()
        val attrs = elem.attributes().asList().map { it.key }
        if (attrs.intersect(ignoreAttributes).isNotEmpty()) continue
        else {
            queue.addAll(elem.children())
        }
        for (flag in flags) {
            if (elem.hasClass(flag.trim())) {
                val code = elem.wholeText().trimIndent()
                snippets.add(code)
                break
            }
        }
    }
    return snippets
}