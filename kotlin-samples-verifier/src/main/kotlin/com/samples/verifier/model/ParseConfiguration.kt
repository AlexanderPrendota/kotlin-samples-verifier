package com.samples.verifier.model

class ParseConfiguration() {
  /**
   * hashset of flags for runnable code snippets (classes for HTML or meta-information for MD)
   */
  var snippetFlags: HashSet<String> = hashSetOf()

  /**
   * hashset of html attributes so tags with them are ignored
   */
  var ignoreAttributes: HashSet<Attribute>? = null

  /**
   * parse directories that match regexp
   */
  var parseDirectory: Regex? = null

  /**
   * ignore directories that match regexp
   */
  var ignoreDirectory: Regex? = null

  /**
   * hashset of html tags to be accepted as code snippets, works for both html and md
   * default (code) for MD so only fencedCodeBlocks are accepted as code snippets
   */
  var parseTags: HashSet<String>? = null

  /**
   * User filter for tag containing snippet
   * Special the word #tag means tag name.
   * Attribute name can implicit transform to true if the attribute exists else false.
   *
   * e.g. (#tag="code" & kotlin-runnable="true" & kotlin-min-compiler-version)
   *
   * Here kotlin-min-compiler-version implicit transform to true  if the attribute exists.
   * The filter supports !, & and | operations.
   */
  var tagFilter: String = ""

  /**
   * User filter for ignoring of tag including inners tags.
   */
  var ignoreTagFilter: String = ""


  /**
   * Indicates whether only errors with severity level `ERROR` should be considered
   * during the verification process.
   *
   * If set to `true`, all other severities such as `INFO` and `WARNING` will be ignored.
   * This ensures that only critical issues affecting the execution context are reported.
   *
   * By default, this is set to `false`, which means errors of all severities are processed.
   */
  var reportErrorOnly: Boolean = false

  constructor(block: ParseConfiguration.() -> Unit) : this() {
    this.block()
  }
}

data class Attribute(val name: String, val value: String)