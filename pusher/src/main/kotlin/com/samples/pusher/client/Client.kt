package com.samples.pusher.client

import com.samples.pusher.core.SamplesPusher
import com.samples.verifier.SamplesVerifier
import com.samples.verifier.SamplesVerifierFactory
import com.samples.verifier.model.Attribute
import com.samples.verifier.model.CollectionOfRepository
import com.sampullara.cli.Args
import org.apache.log4j.BasicConfigurator
import org.apache.log4j.PropertyConfigurator
import kotlin.system.exitProcess

class Client {
  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      BasicConfigurator.configure()
      PropertyConfigurator.configure("log4j.properties")

      val pusherOptions = PusherOptions()
      val checkOptions = CheckOptions()
      try {
        val remainArgs = Args.parse(pusherOptions, args, false)
        Args.parse(checkOptions, remainArgs.toTypedArray())
      } catch (e: Exception) {
        System.err.println(e.message)
        exitProcess(1)
      }

      try {
        val repoSamples = collect(checkOptions)
        SamplesPusher(pusherOptions.repositoryUrl, pusherOptions.path, pusherOptions.username, pusherOptions.passw)
          .readConfigFromFile("config.properties")
          .push(repoSamples)

      } catch (e: Exception) { // TODO
        System.err.println(e.message)
        exitProcess(1)
      }

    }

    private fun collect(options: CheckOptions): CollectionOfRepository {
      val samplesVerifier = helperGetVerifier(options)
      val commits = options.commits
      return samplesVerifier.collect(
        options.repositoryUrl,
        options.branch,
        options.fileType,
        if (commits.size > 0 && commits[0]?.isNotEmpty() == true) commits[0] else null,
        if (commits.size == 2 && commits[1]?.isNotEmpty() == true) commits[1] else null
      )
    }

    private fun <T : CheckOptions> helperGetVerifier(options: T): SamplesVerifier {
      try {
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
          parseDirectory = options.parseDirectory?.let { Regex(it) }
          ignoreDirectory = options.ignoreDirectory?.let { Regex(it) }
          parseTags = options.parseTags?.toHashSet()
        }
    }

  }
} // Client