package com.samples.verifier.internal.utils

import com.samples.verifier.Code
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

internal fun processHTMLFile(
  file: File,
  parseConfiguration: ParseConfiguration
): List<Code> {
  return processHTMLText(file.readText(), parseConfiguration)
}

internal fun processMarkdownFile(
  file: File,
  parseConfiguration: ParseConfiguration
): List<Code> {
  val options = MutableDataSet()
  val parser = Parser.builder(options).build()
  val node: Node = parser.parse(file.readText())
  val render = HtmlRenderer.builder(options).build()
  val htmlText = render.render(node)
  return processHTMLText(htmlText, parseConfiguration)
}

private fun processHTMLText(
  text: String,
  parseConfiguration: ParseConfiguration
): List<Code> {
  val document = Jsoup.parse(text)
  val snippets = mutableListOf<Code>()
  val queue = LinkedList<Element>()
  queue.addFirst(document.body())
  with(parseConfiguration) {
    while (queue.isNotEmpty()) {
      val elem = queue.remove()
      val attrs = elem.attributes().map { Attribute(it.key, it.value) }.toHashSet()

      if (ignoreAttributes != null && attrs.intersect(ignoreAttributes!!).isNotEmpty())
        continue

      queue.addAll(elem.children())

      if (parseTags != null && elem.tagName() !in parseTags!!)
        continue
      if (elem.classNames().intersect(snippetFlags).isNotEmpty()) {
        val code = elem.wholeText().trimIndent()
        snippets.add(code)
      }
    }
  }
  return snippets
}