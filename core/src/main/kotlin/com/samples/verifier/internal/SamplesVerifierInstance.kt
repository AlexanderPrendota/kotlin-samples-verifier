package com.samples.verifier.internal

import com.samples.verifier.*
import com.samples.verifier.internal.utils.ExecutionHelper
import com.samples.verifier.internal.utils.cloneRepository
import com.samples.verifier.internal.utils.processHTMLFile
import com.samples.verifier.internal.utils.processMarkdownFile
import com.samples.verifier.model.ExecutionResult
import com.samples.verifier.model.ParseConfiguration
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException

internal class SamplesVerifierInstance(compilerUrl: String, kotlinEnv: KotlinEnv) : SamplesVerifier {
  private val logger = LoggerFactory.getLogger("Samples Verifier")
  private val executionHelper = ExecutionHelper(compilerUrl, kotlinEnv)

  override var configuration: ParseConfiguration = ParseConfiguration()

  override fun configure(block: ParseConfiguration.() -> Unit): SamplesVerifier {
    configuration.block()
    return this
  }

  override fun collect(url: String, branch: String, type: FileType): Map<Code, ExecutionResult> =
    processRepository(url, branch, type).associate { it.code to executionHelper.executeCode(it) }

  override fun check(url: String, branch: String, type: FileType) {
    var fail = false
    val snippets = processRepository(url, branch, type)
    for (codeSnippet in snippets) {
      val result = executionHelper.executeCode(codeSnippet)
      val errors = result.errors
      if (errors.isNotEmpty()) {
        fail = true
        logger.error("Filename: ${codeSnippet.filename}")
        logger.error("Code: \n${codeSnippet.code}")
        logger.error("Errors: \n${errors.joinToString("\n")}")
      }
    }
    if (fail) throw SamplesVerifierExceptions("Verification failed. Please see errors logs.")
  }

  override fun <T> parse(url: String, branch: String, type: FileType, processResult: (CodeSnippet) -> T): Map<Code, T> =
    processRepository(url, branch, type).associate { it.code to processResult(it) }

  override fun <T> parse(url: String, branch: String, type: FileType, processResult: (List<CodeSnippet>) -> T): T {
    val snippets = processRepository(url, branch, type)
    return processResult(snippets)
  }

  private fun processRepository(url: String, branch: String, type: FileType): List<CodeSnippet> {
    val dir = File(url.substringAfterLast('/').substringBeforeLast('.'))
    return try {
      logger.info("Cloning repository...")
      cloneRepository(dir, url, branch)
      return processFiles(dir, type)
    } catch (e: GitException) {
      logger.error("${e.message}")
      emptyList()
    } catch (e: IOException) {
      logger.error("${e.message}")
      emptyList()
    } finally {
      if (dir.isDirectory) {
        FileUtils.deleteDirectory(dir)
      } else {
        dir.delete()
      }
    }
  }

  private fun processFiles(directory: File, type: FileType): List<CodeSnippet> {
    val snippets = mutableListOf<CodeSnippet>()
    val ignoreRegex = configuration.ignoreDirectory?.let { Regex(it.pattern + File.separator + ".*") }
    val predicate: (String) -> Boolean = if (configuration.parseDirectory != null) {
      { Regex(configuration.parseDirectory!!.pattern + File.separator + ".*").matches(it) }
    } else {
      { true }
    }
    val fileTree = directory.walkTopDown()
      .onEnter { (configuration.ignoreDirectory == null || ignoreRegex?.matches(it.toString()) != true) }
    for (file in fileTree) {
      val path = file.toPath()
      val dir = directory.toPath().relativize(path).toString()
      if (!predicate(dir)) continue
      val fileSnippets = when (type) {
        FileType.MD -> {
          if (file.extension == "md") {
            logger.info("Processing ${file}...")
            processMarkdownFile(file, configuration)
          } else emptyList()
        }
        FileType.HTML -> {
          if (file.extension == "html") {
            logger.info("Processing ${file}...")
            processHTMLFile(file, configuration)
          } else emptyList()
        }
      }
      snippets.addAll(fileSnippets.withIndex().map { code ->
        CodeSnippet("${file.nameWithoutExtension}_${code.index}", code.value)
      })

    }
    return snippets
  }
}