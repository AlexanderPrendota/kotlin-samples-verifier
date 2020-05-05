package com.samples.verifier

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SamplesVerifierTest {
  private val samplesVerifier = SamplesVerifierFactory.create().configure {
    snippetFlags = hashSetOf("run-kotlin")
  }

  private val codeSnippetsFromRepo = run {
    val expectedResult = mutableListOf(
      "fun main() {\n    println(\"Hello world!\")\n}"
    )
    for (i in 2..5) {
      with(expectedResult) {
        add("fun main() {\n    println(\"Hello world!$i.1\")\n}")
        add("fun main() {\n    println(\"Hello world!$i.2\")\n}")
      }
    }
    expectedResult
  }

  @Test
  fun `parse with list test`() {
    samplesVerifier.configure {
      parseDirectory = Regex("core/src/test")
      ignoreDirectory = Regex("core/src/test/resources/ignore_dir")
    }
    val results = listOf(FileType.MD, FileType.HTML).map {
      samplesVerifier.parse(
        "https://github.com/AlexanderPrendota/kotlin-samples-verifier.git",
        "tests",
        it
      ) { snippets: List<CodeSnippet> ->
        snippets.map { it.code }.sorted()
      }
    }
    val expectedResult = codeSnippetsFromRepo.sorted()
    Assertions.assertEquals(listOf(expectedResult, expectedResult), results)
  }

  @Test
  fun `parse test`() {
    samplesVerifier.configure {
      ignoreDirectory = null
      parseDirectory = Regex("core/src/test/resources/testdir")
    }
    val results = listOf(FileType.MD, FileType.HTML).map {
      samplesVerifier.parse(
        "https://github.com/AlexanderPrendota/kotlin-samples-verifier.git",
        "tests",
        it
      ) { codeSnippet: CodeSnippet ->
        codeSnippet.code
      }.toList()
    }
    val expectedResult =
      codeSnippetsFromRepo.filter { it.contains("4") || it.contains("5") }.sorted().map { it to it }
    Assertions.assertEquals(
      listOf(1, 2).map { expectedResult },
      results.map { it.sortedBy { it.first } }
    )
  }

  @Test
  fun `test collect with list`() {
    samplesVerifier.configure {
      ignoreDirectory = Regex("core/src/test/resources/testdir")
      parseDirectory = Regex("core/src/test/resources")
    }
    val result = samplesVerifier.parse(
      "https://github.com/AlexanderPrendota/kotlin-samples-verifier.git",
      "tests",
      FileType.MD,
      listOf(
        "core/src/test/resources/testfile_3.md",
        "core/src/test/resources/testdir/testfile_4.md"
      )
    ) {
      it.code
    }.toList()

    val expectedResult =
      codeSnippetsFromRepo.filter { it.contains("3") }.sorted().map { it to it }
    Assertions.assertEquals(
      expectedResult ,
      result.sortedBy { it.first }
    )
  }
}