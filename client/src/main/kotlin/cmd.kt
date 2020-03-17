import com.samples.verifier.SamplesVerifierFactory
import com.samples.verifier.internal.setConfiguration
import org.apache.log4j.BasicConfigurator
import java.net.MalformedURLException
import org.eclipse.jgit.transport.URIish


fun main(args: Array<String>) {
    BasicConfigurator.configure()
    if (args.size < 2) {
        print("Invalid arguments")
        return
    }
    val flags = mutableListOf<String>()
    for (i in 1 until args.size) flags.add(args[i])
    val repositoryURL = try {
        URIish(args[0])
    } catch (e: MalformedURLException) {
        println("Invalid repository URL")
        return
    }
    val config = setConfiguration(repositoryURL) {
        this.flags = flags
    }
    print(
        SamplesVerifierFactory.create(config)
            .run().results.entries.joinToString { "${it.key} -----to----- ${it.value}\n" })
}