package com.samples.verifier.internal.utils

import com.samples.verifier.Code
import com.samples.verifier.FileType
import com.samples.verifier.model.Attribute
import com.samples.verifier.model.ParseConfiguration
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.data.MutableDataSet
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.File
import java.util.*

private val parseOptions = MutableDataSet()
private val htmlRenderer = HtmlRenderer.builder(parseOptions).build()
private val htmlParser = Parser.builder(parseOptions).build()

internal fun processHTMLFile(file: File, parseConfiguration: ParseConfiguration): List<Code> {
  return processHTMLText(file.readText(), parseConfiguration, FileType.HTML)
}

internal fun processMarkdownFile(file: File, parseConfiguration: ParseConfiguration): List<Code> {
  val node: Node = htmlParser.parse(file.readText())
  val htmlText = htmlRenderer.render(node)
  return processHTMLText(htmlText, parseConfiguration, FileType.MD)
}

private fun processHTMLText(text: String, parseConfiguration: ParseConfiguration, fileType: FileType): List<Code> {
  val document = Jsoup.parse(text)
  val snippets = mutableListOf<Code>()
  val queue = LinkedList<Element>()
  val codeFlags = if (fileType == FileType.MD) {
    parseConfiguration.snippetFlags.map { "language-$it" }
  } else parseConfiguration.snippetFlags
  queue.addFirst(document.body())
  with(parseConfiguration) {
    while (queue.isNotEmpty()) {
      val elem = queue.remove()
      val attrs = elem.attributes().map { Attribute(it.key, it.value) }.toHashSet()

      if (ignoreAttributes != null && attrs.intersect(ignoreAttributes!!).isNotEmpty())
        continue

      queue.addAll(elem.children())

      if ((parseTags != null && (elem.tagName() !in parseTags!!)) ||
        (parseTags == null && (elem.tagName() != "code") && fileType == FileType.MD)
      ) {
        continue
      }
      if (elem.tagName() == "code") {
        if (elem.classNames().intersect(codeFlags).isNotEmpty()) {
          val code = elem.wholeText().trimIndent()
          snippets.add(code)
        }
      } else {
        if (elem.classNames().intersect(snippetFlags).isNotEmpty()) {
          val code = elem.wholeText().trimIndent()
          snippets.add(code)
        }
      }
    }
  }
  return snippets
}