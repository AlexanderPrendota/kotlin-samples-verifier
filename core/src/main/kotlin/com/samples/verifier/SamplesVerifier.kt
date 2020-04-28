package com.samples.verifier

import com.samples.verifier.model.Attribute
import com.samples.verifier.model.ExecutionResult

interface SamplesVerifier {
  /**
   * Collect execution results for code snippets from a git repository
   *
   * @param url git repository url
   * @param branch can be specified as ref name (refs/heads/master),
   *               branch name (master) or tag name (v1.2.3).
   * @param attributes hashset of attributes (classes for HTML or meta-information for MD)
   * @param ignoreAttributes hashset of html attributes so tags with them are ignored
   * @param type [FileType]
   * @return map with code as keys and results as values
   * @throws CallException
   */
  fun collect(
    url: String,
    branch: String,
    attributes: HashSet<String>,
    ignoreAttributes: HashSet<Attribute>,
    type: FileType
  ): Map<Code, ExecutionResult>

  /**
   * Execute code snippets from a git repository
   *
   * @param url git repository url
   * @param branch can be specified as ref name (refs/heads/master),
   *               branch name (master) or tag name (v1.2.3).
   * @param attributes hashset of attributes (classes for HTML or meta-information for MD)
   * @param ignoreAttributes hashset of html attributes so tags with them are ignored
   * @param type [FileType]
   * @throws CallException
   */
  fun check(
    url: String,
    branch: String,
    attributes: HashSet<String>,
    ignoreAttributes: HashSet<Attribute>,
    type: FileType
  )

  /**
   * Parse code snippets from a git repository and process them using [processResult] function.
   *
   * @param url git repository url
   * @param branch can be specified as ref name (refs/heads/master),
   *               branch name (master) or tag name (v1.2.3).
   * @param attributes hashset of attributes (classes for HTML or meta-information for MD)
   * @param ignoreAttributes hashset of html attributes so tags with them are ignored
   * @param type [FileType]
   * @param processResult function to process snippet of code
   * @return map with code snippets as keys and results from [processResult] as values
   * @throws CallException
   */
  fun <T> parse(
    url: String,
    branch: String,
    attributes: HashSet<String>,
    ignoreAttributes: HashSet<Attribute>,
    type: FileType,
    processResult: (CodeSnippet) -> T
  ): Map<Code, T>

  /**
   * Parse code snippets from a git repository and process them using [processResult] function.
   *
   * @param url git repository url
   * @param branch can be specified as ref name (refs/heads/master),
   *               branch name (master) or tag name (v1.2.3).
   * @param attributes hashset of attributes (classes for HTML or meta-information for MD)
   * @param ignoreAttributes hashset of html attributes so tags with them are ignored
   * @param type [FileType]
   * @param processResult function to process list of code snippets
   * @return result of [processResult]
   * @throws CallException
   */
  fun <T> parse(
    url: String,
    branch: String,
    attributes: HashSet<String>,
    ignoreAttributes: HashSet<Attribute>,
    type: FileType,
    processResult: (List<CodeSnippet>) -> T
  ): T
}

enum class FileType {
  MD,
  HTML
}

typealias Code = String

data class CodeSnippet(val filename: String, val code: Code)