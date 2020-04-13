import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.samples.verifier.KotlinEnv
import com.samples.verifier.FileType
import com.samples.verifier.SamplesVerifierFactory
import com.sampullara.cli.Args
import com.sampullara.cli.Argument
import org.apache.log4j.BasicConfigurator
import java.io.FileWriter
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
            try {
                Args.parse(options, args)
            } catch (e: Exception) {
                System.err.println(e.message)
                exitProcess(1)
            }

            val samplesVerifier =
                SamplesVerifierFactory.create(compilerUrl = options.compilerUrl, kotlinEnv = options.kotlinEnv)

            samplesVerifier.check(options.repositoryUrl, options.attributes.toList(), options.fileType)
        }

        private fun collect(args: Array<String>) {
            val options = CollectOptions()
            try {
                Args.parse(options, args)
            } catch (e: Exception) {
                System.err.println(e.message)
                exitProcess(1)
            }
            val samplesVerifier =
                SamplesVerifierFactory.create(compilerUrl = options.compilerUrl, kotlinEnv = options.kotlinEnv)

            FileWriter(options.out).use {
                val mapper = jacksonObjectMapper()
                val results =
                    samplesVerifier.collect(options.repositoryUrl, options.attributes.toList(), options.fileType)
                it.write(mapper.writeValueAsString(results))
            }
        }
    }

    class CollectOptions : CheckOptions() {
        @set:Argument("out", alias = "o", required = true, description = "Filename to store results")
        lateinit var out: String
    }

    open class CheckOptions : CompilerOptions() {
        @set:Argument(
            "repository",
            alias = "r",
            required = true,
            description = "Git repository URL with samples to execute"
        )
        lateinit var repositoryUrl: String

        @set:Argument(
            "attributes",
            alias = "a",
            required = true,
            delimiter = ",",
            description = "Attributes for code snippets"
        )
        lateinit var attributes: Array<String>

        @set:Argument("file-type", required = true, description = "MD or HTML (type of files to be processed)")
        lateinit var fileType: FileType
    }

    open class CompilerOptions {
        @set:Argument("compiler-url", description = "Kotlin compiler URL")
        var compilerUrl: String = "http://localhost:8080/"

        @set:Argument("kotlin-env", description = "JS or JVM")
        var kotlinEnv: KotlinEnv = KotlinEnv.JVM
    }
}
