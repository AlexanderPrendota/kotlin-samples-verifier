import com.samples.verifier.KotlinEnv
import com.samples.verifier.FileType
import com.samples.verifier.internal.utils.ExecutionHelper
import com.samples.verifier.internal.utils.processFile
import com.samples.verifier.model.ExecutionResult
import org.apache.log4j.BasicConfigurator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.io.File


class ParseFileTest {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Test
    fun `base md jvm test`() {
        BasicConfigurator.configure()
        val executionHelper = ExecutionHelper("http://localhost:8080/", KotlinEnv.JVM, logger)
        processFile(
            File("src/test/resources/hello_world.md"),
            FileType.MD,
            listOf("run-kotlin"),
            executionHelper
        )
        val result = executionHelper.results.entries.toList()[0]
        val expectedCode = "fun main() {\n    println(\"Hello world!\")\n}"
        val expectedResult = ExecutionResult(
            mapOf("hello_world_1.kt" to emptyList()),
            null, "<outStream>Hello world!\n</outStream>"
        )
        assertEquals(expectedResult, result.key)
        assertEquals(expectedCode, result.value)
    }
}