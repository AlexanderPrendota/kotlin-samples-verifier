package com.samples.verifier.internal

import com.samples.verifier.*
import com.samples.verifier.internal.utils.ExecutionHelper
import com.samples.verifier.internal.utils.cloneRepository
import com.samples.verifier.internal.utils.processHTMLFile
import com.samples.verifier.internal.utils.processMarkdownFile
import com.samples.verifier.model.Attribute
import com.samples.verifier.model.ExecutionResult
import com.samples.verifier.model.ParseConfiguration
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

internal class SamplesVerifierInstance(compilerUrl: String, kotlinEnv: KotlinEnv) : SamplesVerifier {
  private val logger = LoggerFactory.getLogger("Samples Verifier")
  private val executionHelper = ExecutionHelper(compilerUrl, kotlinEnv)
  override fun collect(
    url: String,
    branch: String,
    attributes: HashSet<String>,
    ignoreAttributes: HashSet<Attribute>,
    type: FileType
  ): Map<Code, ExecutionResult> = processRepository(url, branch, attributes, ignoreAttributes, type)
    .associate { it.code to executionHelper.executeCode(it) }

  override fun check(
    url: String,
    branch: String,
    attributes: HashSet<String>,
    ignoreAttributes: HashSet<Attribute>,
    type: FileType
  ) {
    var fail = false
    val snippets = processRepository(url, branch, attributes, ignoreAttributes, type)
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

  override fun <T> parse(
    url: String,
    branch: String,
    attributes: HashSet<String>,
    ignoreAttributes: HashSet<Attribute>,
    type: FileType,
    processResult: (CodeSnippet) -> T
  ): Map<Code, T> = processRepository(url, branch, attributes, ignoreAttributes, type)
    .associate { it.code to processResult(it) }

  override fun <T> parse(
    url: String,
    branch: String,
    attributes: HashSet<String>,
    ignoreAttributes: HashSet<Attribute>,
    type: FileType,
    processResult: (List<CodeSnippet>) -> T
  ): T {
    val snippets = processRepository(url, branch, attributes, ignoreAttributes, type)
    return processResult(snippets)
  }

  private fun processRepository(
    url: String,
    branch: String,
    attributes: HashSet<String>,
    ignoreAttributes: HashSet<Attribute>,
    type: FileType
  ): List<CodeSnippet> {
    val dir = File(url.substringAfterLast('/').substringBeforeLast('.'))
    return try {
      logger.info("Cloning repository...")
      cloneRepository(dir, url, branch)
      return processFiles(
        dir, type,
        ParseConfiguration().apply {
          snippetFlags = attributes
          this.ignoreAttributes = ignoreAttributes
        }
      )
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

  private fun processFiles(
    directory: File,
    type: FileType,
    parseConfiguration: ParseConfiguration
  ): List<CodeSnippet> {
    val snippets = mutableListOf<CodeSnippet>()
    if (type == FileType.MD) {
      parseConfiguration.apply {
        snippetFlags = snippetFlags.map { "language-$it" }.toHashSet()
        parseTags = parseTags?.plus("code")?.toHashSet() ?: hashSetOf(("code"))
      }
    }
    Files.walk(directory.toPath()).use {
      it.forEach { path: Path ->
        val file = path.toFile()
        val fileSnippets = when (type) {
          FileType.MD -> {
            if (file.extension == "md") {
              logger.info("Processing ${file}...")
              processMarkdownFile(file, parseConfiguration)
            } else emptyList()
          }
          FileType.HTML -> {
            if (file.extension == "html") {
              logger.info("Processing ${file}...")
              processHTMLFile(file, parseConfiguration)
            } else emptyList()
          }
        }
        snippets.addAll(fileSnippets.withIndex().map {
          CodeSnippet("${file.nameWithoutExtension}_${it.index}", it.value)
        })
      }
    }
    return snippets
  }
}