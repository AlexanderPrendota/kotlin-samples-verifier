import com.samples.verifier.CompilerType
import com.samples.verifier.FileType
import com.samples.verifier.internal.utils.RequestHelper
import com.samples.verifier.internal.utils.processFile
import com.samples.verifier.model.ExecutionResult
import org.apache.log4j.BasicConfigurator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File


class ParseFileTest {
    @BeforeEach
    private fun configureLog() = BasicConfigurator.configure()


    @Test
    fun `base md jvm test`() {
        val requestHelper = RequestHelper("http://localhost:8080/", CompilerType.JVM)
        processFile(
            File("src/test/resources/hello_world.md"),
            FileType.MARKDOWN,
            listOf("run-kotlin"),
            requestHelper
        )
        val result = requestHelper.results.entries.toList()[0]
        val expectedCode = "fun main() {\n    println(\"Hello world!\")\n}"
        val expectedResult = ExecutionResult(mapOf("hello_world_1.kt" to emptyList()),
            null, "<outStream>Hello world!\n</outStream>")
        assertEquals(expectedResult, result.key)
        assertEquals(expectedCode, result.value)
    }
}