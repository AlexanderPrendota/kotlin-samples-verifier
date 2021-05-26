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
      val verifierOptions = CheckOptions()
      try {
        val remainArgs = Args.parse(pusherOptions, args, false)
        Args.parse(verifierOptions, remainArgs.toTypedArray())
      } catch (e: Exception) {
        System.err.println(e.message)
        exitProcess(1)
      }

      try {
        val verifier = helperCreateVerifier(verifierOptions)
        val pusher = helperCreatePusher(pusherOptions)

        // work through io
        val isOk = if (!pusherOptions.ioEvent.isNullOrBlank()) {
          val input = System.`in`.bufferedReader().use { it.readText() }
          val eventType = EventType.valueOf(pusherOptions.ioEvent ?: "")
          GitEventHandler(verifier, pusher, verifierOptions).process(eventType, input)
        } else { // work through cli arguments
          val repoSamples = collect(verifier, verifierOptions)
          pusher.push(repoSamples, pusherOptions.createIssue)
          true // collect mode
        }

        if (!isOk)
          exitProcess(2)
      } catch (e: Exception) {
        System.err.println(e.message)
        exitProcess(1)
      }

    }

    private fun collect(samplesVerifier: SamplesVerifier, options: CheckOptions): CollectionOfRepository {
      val commits = options.commits
      return samplesVerifier.collect(
        options.repositoryUrl,
        options.branch,
        options.fileType,
        if (commits.size > 0 && commits[0]?.isNotEmpty() == true) commits[0] else null,
        if (commits.size == 2 && commits[1]?.isNotEmpty() == true) commits[1] else null
      )
    }

    private fun helperCreatePusher(pusherOptions: PusherOptions): SamplesPusher {
      val pusher = SamplesPusher(
        url = pusherOptions.repositoryUrl,
        path = pusherOptions.path,
        user = pusherOptions.username,
        password = pusherOptions.passw,
        templatePath = pusherOptions.templatePath
      )
        .readConfigFromFile(pusherOptions.configPath)
        .configure {
          severity = pusherOptions.severity
        }
      return pusher
    }

    private fun helperCreateVerifier(options: CheckOptions): SamplesVerifier {
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
          tagFilter = options.tagFilter
          ignoreTagFilter = options.ignoreTagFilter
        }
    }

  }
} // Client