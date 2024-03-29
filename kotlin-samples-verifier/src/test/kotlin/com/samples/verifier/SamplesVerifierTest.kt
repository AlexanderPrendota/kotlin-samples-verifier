package com.samples.verifier

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertTrue
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
  fun `test parse with list`() {
    samplesVerifier.configure {
      ignoreDirectory = Regex("src/test/resources/testdir")
      parseDirectory = Regex("src/test/resources")
    }

    val result = samplesVerifier.parse(
      listOf("src/test/resources/md_test.md"),
      FileType.MD
    ) { it }
    assertTrue(result.isNotEmpty())
  }

  @Test
  fun `test collect with list`() {
    samplesVerifier.configure {
      ignoreDirectory = Regex("src/test/resources/testdir")
      parseDirectory = Regex("src/test/resources")
    }

    val result = samplesVerifier.collect(
      listOf("src/test/resources/md_test.md"),
      FileType.MD
    ).map { it.key to it.value }
    assertTrue(result.isNotEmpty())
  }

  @Test
  fun `collect changes test`() {
    samplesVerifier.configure {
      ignoreDirectory = null
      parseDirectory = Regex("core/src/test/resources/testdir")
    }
    val results = listOf(FileType.MD, FileType.HTML).map {
      samplesVerifier.collect(
        "https://github.com/AlexanderPrendota/kotlin-samples-verifier.git",
        "tests",
        it,
        "7a6f08e0fd3020e01f235412183046754779f240",
        "4b33846d2783bfe471a11af3e3fc4ec95b10908f"
      )
    }

    val deletedFilesExpected = listOf<String>("client/src/main/kotlin/cmd.kt",
      "core/src/main/kotlin/com/samples/verifier/SamplesExecutor.kt",
      "core/src/main/kotlin/com/samples/verifier/SamplesParser.kt",
      "core/src/main/kotlin/com/samples/verifier/internal/Config.kt",
      "core/src/main/kotlin/com/samples/verifier/internal/SamplesExecutorInstance.kt",
      "core/src/main/kotlin/com/samples/verifier/internal/SamplesParserInstance.kt").sorted()

    val snippets = results.map { it.snippets.map { it.key.code to it.key.code } }
    val expectedResult =
      codeSnippetsFromRepo.filter { it.contains("4") || it.contains("5") }.sorted().map { it to it }
    Assertions.assertEquals(
      listOf(1, 2).map { expectedResult },
      snippets.map { it.sortedBy { it.first } }
    )

    Assertions.assertEquals(
      listOf(1, 2).map { deletedFilesExpected },
      results.map { it.diff?.deletedFiles?.sorted() }
    )
  }

  @Test
  fun `collect changes between two branches`() {
    samplesVerifier.configure {
      ignoreDirectory = null
      parseDirectory = Regex("core/src/test/resources")
    }
    val results =
      samplesVerifier.collect(
        "https://github.com/AlexanderPrendota/kotlin-samples-verifier.git",
        "tests",
        "https://github.com/AlexanderPrendota/kotlin-samples-verifier.git",
        "test-branch-two",
        FileType.MD
      )

    val deletedFilesExpected = listOf<String>(
      "core/src/test/resources/hello_world.html",
      "core/src/test/resources/hello_world.md"
    ).sorted()

    Assertions.assertEquals(
      deletedFilesExpected,
      results.diff?.deletedFiles?.sorted()
    )
    Assertions.assertEquals(
      results.snippets.size,
      2
    )
  }
}