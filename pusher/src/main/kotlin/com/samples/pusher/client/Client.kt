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

            val options = PusherOptions()
            try {
                Args.parse(options, args)
            } catch (e: Exception) {
                System.err.println(e.message)
                exitProcess(1)
            }

            SamplesPusher(options.repositoryUrl, options.path, options.username, options.passw)
                .readConfigFromFile("config.properties")
                .push(options.input)
        }
    }
} // Client

class PusherOptions : CredentialsOption() {
    @set:Argument("in",
        required = true,
        description = "Filename to read results")
    lateinit var input: String

    @set:Argument(
        "repository",
        alias = "r",
        required = true,
        description = "Git repository to push"
    )
    lateinit var repositoryUrl: String


    @set:Argument(
        "path",
        alias = "p",
        description = "Path relatively a target repository"
    )
    var path: String = ""
}

open class CredentialsOption {
    @set:Argument(
        "username",
        required = true,
        description = "Username or acces token for push to a target repository"
    )
    lateinit var username: String

    @set:Argument(
        "passw",
        description = "User's password for push  to a target repository"
    )
    var passw: String = ""
}

