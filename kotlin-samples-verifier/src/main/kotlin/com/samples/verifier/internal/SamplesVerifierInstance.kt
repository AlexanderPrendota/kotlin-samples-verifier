package com.samples.verifier.internal

import com.samples.verifier.*
import com.samples.verifier.internal.utils.*
import com.samples.verifier.model.CollectionOfRepository
import com.samples.verifier.model.DiffOfRepository
import com.samples.verifier.model.ExecutionResult
import com.samples.verifier.model.ParseConfiguration
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
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

  data class RepoChanges(val diff: DiffOfRepository?, val snippets: List<CodeSnippet>)

  override fun collect(
    url: String,
    branch: String,
    type: FileType,
    startCommit: String?,
    endCommit: String?
  ): CollectionOfRepository {
    return if (startCommit != null || endCommit != null) {
      val changes = processDiffCommits(url, branch, type, startCommit, endCommit)
      CollectionOfRepository(
        url = url,
        branch = branch,
        snippets = changes.snippets.associateWith { executionHelper.executeCode(it) },
        diff = changes.diff
      )
    } else {
      val snippets = processRepository(url, branch, type)
      CollectionOfRepository(url = url,
        branch = branch,
        snippets = snippets.associateWith { executionHelper.executeCode(it) }
      )
    }
  }


  override fun collect(
    baseUrl: String,
    baseBranch: String,
    headUrl: String,
    headBranch: String,
    type: FileType
  ): CollectionOfRepository {
    val changes = processDiffBranches(baseUrl, baseBranch, headUrl, headBranch, type)
    return CollectionOfRepository(
      url = baseUrl,
      branch = baseBranch,
      snippets = changes.snippets.associateWith { executionHelper.executeCode(it) },
      diff = changes.diff
    )
  }

  override fun collect(files: List<String>, type: FileType): Map<Code, ExecutionResult> =
    processFiles(File(""), files, type).associate { it.code to executionHelper.executeCode(it) }

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

  override fun <T> parse(
    url: String,
    branch: String,
    type: FileType,
    processResult: (CodeSnippet) -> T
  ): Map<Code, T> = processRepository(url, branch, type).associate { it.code to processResult(it) }

  override fun <T> parse(
    files: List<String>,
    type: FileType,
    processResult: (CodeSnippet) -> T
  ): Map<Code, T> =
    processFiles(File(""), files, type).associate { it.code to processResult(it) }

  override fun <T> parse(url: String, branch: String, type: FileType, processResult: (List<CodeSnippet>) -> T): T {
    val snippets = processRepository(url, branch, type)
    return processResult(snippets)
  }

  private fun processRepository(
    url: String,
    branch: String,
    type: FileType,
    filenames: List<String>? = null
  ): List<CodeSnippet> {
    return cloneRepositoryToDir(url, branch, false) {git ->
      val repo = git.repository
      if (filenames == null) processFiles(repo.workTree, type)
      else processFiles(repo.workTree, filenames, type)
    }
  }

  private fun processDiffBranches(
    baseUrl: String,
    baseBranch: String,
    headUrl: String,
    headBranch: String,
    type: FileType
  ): RepoChanges {
    return cloneRepositoryToDir(baseUrl, baseBranch, true) { git ->
        logger.info("Getting diff between $baseUrl:$baseBranch and $headUrl:$headBranch")

        val fr = fetch(git, headUrl, headBranch)
        val newName = fr.getTrackingRefUpdates().first().localName

        // git diff $(git-merge-base A B) B
        // aka triple dots diff
        val commonAncestor = mergeBase(git, baseBranch, newName)
        val headCommit = getCommit(git.repository, newName)
        processDiff(git, commonAncestor, headCommit, emptyList(), type)
    }
  }

  private fun <T> cloneRepositoryToDir(
    url: String,
    branch: String,
    bare: Boolean = false,
    cb: (Git) -> T
  ): T {
    val dir = File(url.substringAfterLast('/').substringBeforeLast('.'))
    try {
      logger.info("Cloning repository...")
      return cloneRepository(dir, url, branch, bare).use(cb)
    } catch (e: GitException) {
      logger.error("Git: ${e.message}")
      throw e
    } catch (e: IOException) {
      logger.error("IO: ${e.message}")
      throw e
    }  finally {
      if (dir.isDirectory) {
        FileUtils.deleteDirectory(dir)
      } else {
        dir.delete()
      }
    }
  }

  private fun processDiffCommits(
    url: String,
    branch: String,
    type: FileType,
    startCommitName: String?,
    endCommitName: String?,
    filenames: List<String>? = null
  ): RepoChanges {
    return cloneRepositoryToDir(url, branch, true) { git ->
        logger.info("Getting diff between $startCommitName and ${endCommitName ?: "HEAD"}")
        val st = if (startCommitName == null) null else getCommit(git.repository, startCommitName)
        val end = getCommit(git.repository, endCommitName ?: "HEAD")
        processDiff(git, st, end, filenames, type)
      }
  }

  private fun processDiff(
    git: Git,
    startCommit: RevCommit?,
    endCommit: RevCommit,
    filenames: List<String>?,
    type: FileType
  ): RepoChanges {
    val diff = diff(git, startCommit, endCommit)
    val diffFilenames = getModifiedOrAddedFilenames(diff)
    diffFilenames.forEach { logger.info("File $it is found in commit diff") }
    val allFilenames = diffFilenames + filenames.orEmpty()
    val diffInfo = DiffOfRepository(startCommit?.name ?: "", endCommit.name ?: "HEAD", getDeletedFilenames(diff))
    return RepoChanges(diffInfo, processRepoFiles(git.repository, endCommit, allFilenames, type))
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

  private fun filterFilesDirs(filenames: List<String>): List<String> {
    val fileRegex = configuration.parseDirectory?.separatePattern()
    val ignoreRegex = configuration.ignoreDirectory?.separatePattern()
    return filenames.filter { fileRegex?.matches(it) ?: true && !(ignoreRegex?.matches(it) ?: false) }
  }

  private fun filterFilesType(filenames: List<String>, type: FileType): List<String> {
    return filenames.filter {
      type == FileType.HTML && FilenameUtils.getExtension(it) == "html"
        || type == FileType.MD && FilenameUtils.getExtension(it) == "md"
    }
  }

  private fun processFiles(directory: File, filenames: List<String>, type: FileType): List<CodeSnippet> {
    return filterFilesDirs(filenames).map { File(it) }
      .flatMap { processFile(directory, directory.resolve(it), type) }
  }

  private fun processFile(baseDir: File, file: File, type: FileType): List<CodeSnippet> {
    return when (type) {
      FileType.MD -> {
        if (file.extension == "md") {
          logger.info("Processing ${file.toRelativeString(baseDir)}...")
          processMarkdownFile(file, configuration)
        } else emptyList()
      }
      FileType.HTML -> {
        if (file.extension == "html") {
          logger.info("Processing ${file.toRelativeString(baseDir)}...")
          processHTMLFile(file, configuration)
        } else emptyList()
      }
    }.withIndex().map { code ->
      CodeSnippet(file.toRelativeString(baseDir), code.value)
    }
  }

  private fun processRepoFiles(
    repository: Repository,
    commit: RevCommit,
    filenames: List<String>,
    type: FileType
  ): List<CodeSnippet> {
    return filterFilesType(filterFilesDirs(filenames), type) // we don't need to extract with another extensions
      .flatMap {
        val content = extractFiles(repository, commit, listOf(it)) // extract not all files at once, only one
        processFile(it, content.getOrDefault(it, ""), type)
      }
  }

  private fun processFile(filename: String, content: String, type: FileType): List<CodeSnippet> {
    return when (type) {
      FileType.MD -> {
        if (FilenameUtils.getExtension(filename) == "md") {
          logger.info("Processing ${filename}...")
          processMarkdownText(content, configuration)
        } else emptyList()
      }
      FileType.HTML -> {
        if (FilenameUtils.getExtension(filename) == "html") {
          logger.info("Processing ${filename}...")
          processHTMLText(content, configuration)
        } else emptyList()
      }
    }.withIndex().map { code ->
      CodeSnippet(filename, code.value)
    }
  }

  private fun Regex.separatePattern() = Regex(this.pattern + File.separator + ".*")
}