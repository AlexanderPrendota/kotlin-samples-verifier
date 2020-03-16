import com.samples.verifier.internal.utils.processFile
import org.apache.commons.io.FileUtils
import org.apache.log4j.BasicConfigurator
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File


class ParseFileTest {
    @BeforeEach
    private fun configureLog() = BasicConfigurator.configure()

    @Test
    fun `base parse test`() {
        processFile(
            File("src/test/resources/01_Data_classes.md"),
            "src/test/resources/",
            "src/test/resources/",
            listOf("run-kotlin")
        )
        val expected = File("src/test/resources/01_Data_classes_result.kt")
        val actual = File("src/test/resources/01_Data_classes/01_Data_classes_1.kt")
        assert(FileUtils.contentEquals(expected, actual))
        FileUtils.deleteDirectory(File("src/test/resources/01_Data_classes/"))
    }
}