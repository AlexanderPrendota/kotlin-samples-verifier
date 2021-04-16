package com.samples.verifier.internal

import com.samples.verifier.*
import com.samples.verifier.internal.utils.*
import com.samples.verifier.internal.utils.ExecutionHelper
import com.samples.verifier.internal.utils.cloneRepository
import com.samples.verifier.internal.utils.getCommit
import com.samples.verifier.internal.utils.processHTMLFile
import com.samples.verifier.internal.utils.processMarkdownFile
import com.samples.verifier.model.CollectionOfRepository
import com.samples.verifier.model.DiffOfRepository
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

  override fun collect(url: String, branch: String, type: FileType, startCommit: String?, endCommit: String?): CollectionOfRepository {
  val (diff, snippets) = processRepository(url, branch, type, null, startCommit, endCommit)
  return CollectionOfRepository ( url, branch,
                                  snippets.associate { it.code to executionHelper.executeCode(it) }, diff)
}

  override fun collect(files: List<String>, type: FileType): Map<Code, ExecutionResult> =
    processFiles(File(""), files, type).associate { it.code to executionHelper.executeCode(it) }

  override fun check(url: String, branch: String, type: FileType) {
    var fail = false
    val (_, snippets) = processRepository(url, branch, type)
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
    processRepository(url, branch, type).second.associate { it.code to processResult(it) }

  override fun <T> parse(
    files: List<String>,
    type: FileType,
    processResult: (CodeSnippet) -> T
  ): Map<Code, T> =
    processFiles(File(""), files, type).associate { it.code to processResult(it) }

  override fun <T> parse(url: String, branch: String, type: FileType, processResult: (List<CodeSnippet>) -> T): T {
    val (_, snippets) = processRepository(url, branch, type)
    return processResult(snippets)
  }

  private fun processRepository(
    url: String,
    branch: String,
    type: FileType,
    filenames: List<String>? = null,
    startCommit: String? = null,
    endCommit: String? = null
  ): Pair<DiffOfRepository?, List<CodeSnippet>> {
    val dir = File(url.substringAfterLast('/').substringBeforeLast('.'))

    var allFilenames :List<String>? = filenames
    var diffInfo:DiffOfRepository? = null
    return try {
      logger.info("Cloning repository...")
      cloneRepository(dir, url, branch).use {
        if( startCommit!= null) {
          logger.info("Getting diff between $startCommit and ${endCommit ?: "HEAD"}")
          val st = getCommit(it.repository, startCommit)
          val end = getCommit(it.repository, endCommit ?: "HEAD")
          val diff  = diff(it, st, end)
          allFilenames = getModifiedOrAddedFilenames(diff).map {  it  } + allFilenames.orEmpty()
          diffInfo = DiffOfRepository(startCommit, endCommit?: "HEAD", deletedFiles = getDeletedFilenames(diff) )
        }
      }

      allFilenames?.forEach {logger.info("File ${it} is found in commit diff")}

      return if (allFilenames == null) Pair(diffInfo, processFiles(dir, type))
      else Pair(diffInfo, processFiles(dir, allFilenames!!, type))
    } catch (e: GitException) {
      logger.error("${e.message}")
      Pair(null, emptyList())
    } catch (e: IOException) {
      logger.error("${e.message}")
      Pair(null, emptyList())
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
    val fileRegex = configuration.parseDirectory?.let {
      Regex(it.pattern + File.separator + ".*")
    }
    val fileTree = directory.walkTopDown()
      .onEnter {
        (configuration.ignoreDirectory == null ||
          configuration.ignoreDirectory?.matches(it.toRelativeString(directory)) != true)
      }
    for (file in fileTree) {
      val dir = file.relativeTo(directory).toString()
      if (fileRegex?.matches(dir) == false) continue
      snippets.addAll(processFile(directory, file, type))
    }
    return snippets
  }

  private fun processFiles(directory: File, filenames: List<String>, type: FileType): List<CodeSnippet> {
    val fileRegex = configuration.parseDirectory?.separatePattern()
    val ignoreRegex = configuration.ignoreDirectory?.separatePattern()
    return filenames
      .filter { fileRegex?.matches(it) != false && ignoreRegex?.matches(it) != true }
      .map { File(it) }
      .flatMap { processFile(directory, directory.resolve(it), type) }
  }

  private fun processFile(baseDir:File, file: File, type: FileType): List<CodeSnippet> {
    return when (type) {
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
    }.withIndex().map { code ->
      CodeSnippet(file.toRelativeString(baseDir), code.value)
    }
  }

  private fun Regex.separatePattern() = Regex(this.pattern + File.separator + ".*")
}