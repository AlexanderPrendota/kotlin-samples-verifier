package com.samples.pusher.client

import com.samples.verifier.FileType
import com.samples.verifier.KotlinEnv
import com.samples.verifier.model.ProjectSeveriry
import com.sampullara.cli.Argument


/***
 *
 * [+] Options for pusher
 */

class PusherOptions : CredentialsOption() {

  @set:Argument(
    "io-event",
    description = "Special arguments  for GitHub event"
  )
  var ioEvent: String? = null

  @set:Argument(
    "push-repository",
    alias = "pr",
    required = true,
    description = "Git repository URL to push the samples"
  )
  lateinit var repositoryUrl: String

  @set:Argument(
    "push-path",
    alias = "p",
    description = "Path relatively a push repository"
  )
  var path: String = ""

  @set:Argument(
    "template-path",
    description = "Url or file path for loading templates"
  )
  var templatePath: String = "templates"

  @set:Argument(
    "config-path",
    description = "Url or file path for loading config"
  )
  var configPath: String = "config.properties"

  @set:Argument(
    "severity",
    description = "Create issue and do not push if " +
      "the snippet has errors equals or greater the severity"
  )
  var severity: ProjectSeveriry = ProjectSeveriry.ERROR
}

open class CredentialsOption {
  @set:Argument(
    "username",
    required = true,
    description = "Username or access token for push to a target repository"
  )
  lateinit var username: String

  @set:Argument(
    "passw",
    description = "User's password for push  to a target repository"
  )
  var passw: String = ""
}

// [-] Options for pusher


/***
 *
 * [+] Options for verifier
 */

open class CheckOptions : ParseOptions() {
  @set:Argument(
    "repository",
    alias = "r",
    required = false,
    description = "Git repository URL with samples to execute"
  )
  lateinit var repositoryUrl: String

  @set:Argument(
    "branch",
    alias = "br",
    description = "git branch of repository to be verified"
  )
  var branch: String = "master"

  @set:Argument(
    value = "commits",
    delimiter = ",",
    description = "Considering only the changed files between two arbitrary commits \"commit1,commit2\"" +
      "or starting from \"commit1\" if commit2 on one side is omitted"
  )
  var commits: Array<String?> = Array<String?>(2) { null }
}

open class ParseOptions : CompilerOptions() {
  @set:Argument(
    "tag-filter",
    required = false,
    description = "User filter for tag containing snippet  like so: (#tag=\"name\" & attr1=\"val\"). " +
      "It also supports !, &, | operations."
  )
  var tagFilter: String = ""

  @set:Argument(
    "ignore-tag-filter",
    required = false,
    description = "User filter for ignoring of tag including inners tags."
  )
  var ignoreTagFilter: String = ""

  @set:Argument(
    "snippet-flags",
    alias = "f",
    required = true,
    delimiter = ",",
    description = "Flags for code snippets, separated by \",\" like so: \"attr1,attr2\""
  )
  lateinit var snippetFlags: Array<String>

  @set:Argument(
    value = "ignore-attributes",
    delimiter = ",",
    description = "Attributes (name and value separated by \\\":\\\" (name:value)) for code snippets to ignore, " +
      "separated by \\\",\\\" like so: \\\"attr1,attr2\\\""
  )
  var ignoreAttributes: Array<String>? = null

  @set:Argument(
    value = "parse-directory",
    description = "Regexp for directories to be processed"
  )
  var parseDirectory: String? = null

  @set:Argument(
    value = "ignore-directory",
    description = "Regexp for directories to be ignored"
  )
  var ignoreDirectory: String? = null

  @set:Argument(
    value = "parse-tags",
    delimiter = ",",
    description = "Html tags to be accepted as code snippets, works for both html and md\n" +
      "default (code) for MD so only fencedCodeBlocks are accepted as code snippets"
  )
  var parseTags: Array<String>? = null

  @set:Argument("file-type", description = "MD or HTML (type of files to be processed)")
  var fileType: FileType = FileType.MD
}

open class CompilerOptions {
  @set:Argument("compiler-url", description = "Kotlin compiler URL")
  var compilerUrl: String = "http://localhost:8080/"

  @set:Argument("kotlin-env", description = "JS or JVM")
  var kotlinEnv: KotlinEnv = KotlinEnv.JVM
}

// [-] Options for verifier