import com.samples.verifier.FileType
import com.samples.verifier.SamplesVerifierFactory
import org.apache.log4j.BasicConfigurator

fun main(args: Array<String>) {
    BasicConfigurator.configure()
    if (args.size < 2) {
        print("Invalid arguments")
        return
    }
    val attributes = mutableListOf<String>()
    for (i in 1 until args.size) attributes.add(args[i])
    print(
        SamplesVerifierFactory.create().collect(
            args[0],
            attributes,
            FileType.MARKDOWN
        ).entries.joinToString { "${it.key} -----to----- ${it.value}\n" })
}