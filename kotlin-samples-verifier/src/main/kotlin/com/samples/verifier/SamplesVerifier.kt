package com.samples.verifier

import com.samples.verifier.model.CollectionOfRepository
import com.samples.verifier.model.ExecutionResult
import com.samples.verifier.model.ParseConfiguration

interface SamplesVerifier {
  /**
   * @see [ParseConfiguration]
   */
  var configuration: ParseConfiguration

  /**
   * Edit [configuration]
   */
  fun configure(block: ParseConfiguration.() -> Unit): SamplesVerifier

  /**
   * Collect execution results for code snippets from a git repository
   * Can consider only changes between the commits
   *
   * @param url git repository url
   * @param branch can be specified as ref name (refs/heads/master),
   *               branch name (master) or tag name (v1.2.3).
   * @param type [FileType]
   * @param startCommit
   * @param endCommit
   * @return [CollectionOfRepository] including map with code as keys and results as values,
   *                                  deleted files between commits
   * @throws CallException
   */
  fun collect(
    url: String,
    branch: String,
    type: FileType,
    startCommit: String? = null,
    endCommit: String? = null
  ): CollectionOfRepository

  /**
   * Collect execution results for code snippets from a changes between two branches.
   * It considers the changes on a head branch, starting at a common ancestor of both branches.
   *
   * @param baseUrl git base repository url
   * @param baseBranch can be specified as ref name (refs/heads/master),
   *               branch name (master) or tag name (v1.2.3).
   * @param headUrl git head repository url
   * @param headBranch can be specified as ref name (refs/heads/master),
   *               branch name (master) or tag name (v1.2.3).
   * @param type [FileType]
   * @return [CollectionOfRepository] including map with code as keys and results as values,
   *                                  deleted files between commits
   * @throws CallException
   */
  fun collect(
    baseUrl: String,
    baseBranch: String,
    headUrl: String,
    headBranch: String,
    type: FileType
  ): CollectionOfRepository

  /**
   * Collect execution results for code snippets from passed files
   *
   * @param files files to be processed
   * @param type [FileType]
   * @return map with code as keys and results as values
   * @throws CallException
   */
  fun collect(files: List<String>, type: FileType): Map<Code, ExecutionResult>

  /**
   * Execute code snippets from a git repository
   *
   * @param url git repository url
   * @param branch can be specified as ref name (refs/heads/master),
   *               branch name (master) or tag name (v1.2.3).
   * @param type [FileType]
   * @throws CallException
   */
  fun check(url: String, branch: String, type: FileType)

  /**
   * Extract code snippets from a git repository and process them using [processResult] function.
   *
   * @param url git repository url
   * @param branch can be specified as ref name (refs/heads/master),
   *               branch name (master) or tag name (v1.2.3).
   * @param type [FileType]
   * @param processResult function to process snippet of code
   * @return map with code snippets as keys and results from [processResult] as values
   * @throws CallException
   */
  fun <T> parse(url: String, branch: String, type: FileType, processResult: (CodeSnippet) -> T): Map<Code, T>

  /**
   * Extract code snippets from passed files and process them using [processResult] function.
   *
   * @param type [FileType]
   * @param files list of filenames to be processed
   * @param processResult function to process snippet of code
   * @return map with code snippets as keys and results from [processResult] as values
   * @throws CallException
   */
  fun <T> parse(
    files: List<String>,
    type: FileType,
    processResult: (CodeSnippet) -> T
  ): Map<Code, T>

  /**
   * Extract code snippets from a git repository and process them using [processResult] function.
   *
   * @param url git repository url
   * @param branch can be specified as ref name (refs/heads/master),
   *               branch name (master) or tag name (v1.2.3).
   * @param type [FileType]
   * @param processResult function to process list of code snippets
   * @return result of [processResult]
   * @throws CallException
   */
  fun <T> parse(url: String, branch: String, type: FileType, processResult: (List<CodeSnippet>) -> T): T
}

enum class FileType {
  MD,
  HTML
}

typealias Code = String

data class CodeSnippet(val filename: String, val code: Code)