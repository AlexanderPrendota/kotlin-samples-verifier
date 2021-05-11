package com.samples.verifier.base

import com.samples.verifier.*
import com.samples.verifier.internal.utils.ExecutionHelper
import com.samples.verifier.internal.utils.processHTMLFile
import com.samples.verifier.internal.utils.processMarkdownFile
import com.samples.verifier.model.ExecutionResult
import com.samples.verifier.model.ParseConfiguration
import com.samples.verifier.model.ProjectSeverity
import org.junit.jupiter.api.Assertions.assertTrue
import java.io.File

open class BaseExecuteFileTest(url: String = DEFAULT_COMPILER_HOST, kotlinEnv: KotlinEnv = KotlinEnv.JVM) {
  private val executionHelper = ExecutionHelper(url, kotlinEnv)

  var defaultConfiguration = ParseConfiguration()

  fun hasNoErrors(file: String, fileType: FileType, parseConfiguration: ParseConfiguration? = null) {
    val results = executeSnippets(file, fileType, parseConfiguration)
    results.map { it.value.hasNoErrors() }
  }

  fun containsOutput(
    file: String,
    fileType: FileType,
    code: Code,
    text: String,
    parseConfiguration: ParseConfiguration? = null
  ) {
    val results = executeSnippets(file, fileType, parseConfiguration)
    (results[code] ?: error("")).contains(text)
  }

  private fun executeSnippets(
    file: String,
    fileType: FileType,
    parseConfiguration: ParseConfiguration?
  ): Map<Code, ExecutionResult> {
    val configuration = parseConfiguration ?: defaultConfiguration
    val snippets = when (fileType) {
      FileType.MD -> processMarkdownFile(
        File(file),
        configuration
      )
      FileType.HTML -> processHTMLFile(
        File(file),
        configuration
      )
    }
    return snippets.associateWith { executionHelper.executeCode(CodeSnippet(file, it)) }
  }
}

fun ExecutionResult.contains(str: String) {
  assertTrue(text.contains(str))
}

fun ExecutionResult.hasNoErrors() {
  assertTrue(errors.all { it.severity != ProjectSeverity.ERROR })
}