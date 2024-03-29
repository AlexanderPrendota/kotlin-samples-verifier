package com.samples.verifier.client

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.samples.verifier.FileType
import com.samples.verifier.KotlinEnv
import com.samples.verifier.SamplesVerifier
import com.samples.verifier.SamplesVerifierFactory
import com.samples.verifier.model.Attribute
import com.sampullara.cli.Args
import com.sampullara.cli.Argument
import org.apache.log4j.BasicConfigurator
import org.apache.log4j.PropertyConfigurator
import java.io.FileWriter
import kotlin.system.exitProcess

class Client {
  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      BasicConfigurator.configure()
      PropertyConfigurator.configure("log4j.properties")
      if (args.isEmpty()) {
        System.err.println("Command is not specified: `check` or `collect` commands are supported.")
        exitProcess(1)
      }
      val command = args[0]
      val restParameters = args.copyOfRange(1, args.size)
      when (command) {
        "check" -> check(restParameters)
        "collect" -> collect(restParameters)
        else -> {
          System.err.println("Unknown command `$command`: `check` or `collect` commands are supported.")
          exitProcess(1)
        }
      }
    }

    private fun check(args: Array<String>) {
      val options = CheckOptions()

      val samplesVerifier = helper(args, options)

      samplesVerifier.check(
        options.repositoryUrl,
        options.branch,
        options.fileType
      )
    }

    private fun collect(args: Array<String>) {
      val options = CollectOptions()

      val samplesVerifier = helper(args, options)

      FileWriter(options.out).use {
        val mapper = jacksonObjectMapper()
        val commits = options.commits
        val results =
          samplesVerifier.collect(
            options.repositoryUrl,
            options.branch,
            options.fileType,
            if (commits.size > 0) commits[0] else null,
            if (commits.size == 2) commits[1] else null
          )
        it.write(mapper.writeValueAsString(results))
      }
    }

    private fun <T : CheckOptions> helper(args: Array<String>, options: T): SamplesVerifier {
      try {
        Args.parse(options, args)
        if (options.commits.size > 2) {
          throw Exception("Commits param is invalid")
        }
      } catch (e: Exception) {
        System.err.println(e.message)
        exitProcess(1)
      }

      return SamplesVerifierFactory.create(compilerUrl = options.compilerUrl, kotlinEnv = options.kotlinEnv)
        .configure {
          snippetFlags = options.snippetFlags.toHashSet()
          ignoreAttributes = options.ignoreAttributes?.map { s ->
            val (a, b) = s.split(":", limit = 2)
            Attribute(a, b)
          }?.toHashSet()
          parseDirectory  = options.parseDirectory?.let { Regex(it) }
          ignoreDirectory = options.ignoreDirectory?.let { Regex(it) }
          parseTags       = options.parseTags?.toHashSet()
          tagFilter       = options.tagFilter
          ignoreTagFilter = options.ignoreTagFilter
        }
    }
  }

  class CollectOptions : CheckOptions() {
    @set:Argument("out", alias = "o", required = true, description = "Filename to store results")
    lateinit var out: String
  }

  open class CheckOptions : ParseOptions() {
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
      "repository",
      alias = "r",
      required = true,
      description = "Git repository URL with samples to execute"
    )
    lateinit var repositoryUrl: String

    @set:Argument(
      "branch",
      alias = "br",
      description = "git branch"
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
}
