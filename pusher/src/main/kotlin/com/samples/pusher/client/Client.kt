package com.samples.pusher.client

import com.samples.pusher.core.SamplesPusher
import com.sampullara.cli.Args
import com.sampullara.cli.Argument
import org.apache.log4j.BasicConfigurator
import kotlin.system.exitProcess

class Client {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            BasicConfigurator.configure()
            if (args.isEmpty()) {
                System.err.println("Command is not specified: `check` or `collect` commands are supported.")
                exitProcess(1)
            }

            val options = PusherOptions()
            try {
                Args.parse(options, args)
            } catch (e: Exception) {
                System.err.println(e.message)
                exitProcess(1)
            }

            SamplesPusher(options.repositoryUrl).push(options.input)
        }
    }
} // Client

class PusherOptions {
    @set:Argument("in", alias = "in", required = true, description = "Filename to read results")
    lateinit var input: String

    @set:Argument(
        "repository",
        alias = "r",
        required = true,
        description = "Git repository to push"
    )
    lateinit var repositoryUrl: String


    @set:Argument(
        "repository",
        alias = "r",
        required = true,
        description = "Git repository to push"
    )
    lateinit var repositoryUrl: String
}


