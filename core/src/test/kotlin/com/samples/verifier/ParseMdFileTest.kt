package com.samples.verifier

import com.samples.verifier.internal.utils.processMarkdownFile
import com.samples.verifier.model.Attribute
import com.samples.verifier.model.ParseConfiguration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class ParseMdFileTest {
  @Test
  fun `process md test`() {
    val helloWorld = """
      fun main() {
          println("Hello world!")
      }
    """.trimIndent()
    val res = processMarkdownFile(
      File("src/test/resources/md_test.md"),
      ParseConfiguration {
        snippetFlags = hashSetOf("kotlin", "run-kotlin")
        ignoreAttributes = hashSetOf(Attribute("data-highlight-only", ""),
          Attribute("another-ignore-attribute", "ignore"))
        parseTags = hashSetOf("code", "div")
      })
    assertEquals(listOf(1, 2, 3, 4, 5, 6, 7).map { helloWorld }, res)
  }
  @Test
  fun `process runnable md test`() {
    val helloWorld = """
      fun main() {
          println("Hello world!")
      }
    """.trimIndent()
    val res = processMarkdownFile(
      File("src/test/resources/md_runnable_test.md"),
      ParseConfiguration {
        tagFilter = "(#tag=\"code\" & kotlin-runnable=\"true\" & kotlin-min-compiler-version)"
        ignoreTagFilter  = "another-ignore-attribute=\"ignore\" | data-highlight-only"
      })
    assertEquals(listOf(1, 2).map { helloWorld }, res)
  }
}