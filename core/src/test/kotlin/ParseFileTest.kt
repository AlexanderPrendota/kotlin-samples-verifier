import com.samples.verifier.FileType
import com.samples.verifier.KotlinEnv
import com.samples.verifier.internal.utils.ExecutionHelper
import com.samples.verifier.internal.utils.processFile
import com.samples.verifier.model.ExecutionResult
import com.samples.verifier.model.KotlinFile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File


class ParseFileTest {

    @Test
    fun `base md jvm test`() {
        val executionHelper = ExecutionHelper("http://localhost:8080/", KotlinEnv.JVM)
        lateinit var result: Pair<ExecutionResult, KotlinFile>
        processFile(
            File("src/test/resources/hello_world.md"),
            FileType.MD,
            listOf("run-kotlin"),
            executionHelper
        ) { res, file -> result = res to file }
        val expectedCode = "fun main() {\n    println(\"Hello world!\")\n}"
        val expectedResult = ExecutionResult(
            mapOf("hello_world_1.kt" to emptyList()),
            null, "<outStream>Hello world!\n</outStream>"
        )
        assertEquals(expectedResult, result.first)
        assertEquals(expectedCode, result.second.text)
    }

    @Test
    fun `base html jvm test`() {
        val executionHelper = ExecutionHelper("http://localhost:8080/", KotlinEnv.JVM)
        lateinit var result: Pair<ExecutionResult, KotlinFile>
        processFile(
            File("src/test/resources/hello_world.html"),
            FileType.HTML,
            listOf("run-kotlin"),
            executionHelper
        ) { res, file -> result = res to file }
        val expectedCode = "fun main() {\n    println(\"Hello world!\")\n}"
        val expectedResult = ExecutionResult(
            mapOf("hello_world_1.kt" to emptyList()),
            null, "<outStream>Hello world!\n</outStream>"
        )
        assertEquals(expectedResult, result.first)
        assertEquals(expectedCode, result.second.text)
    }
}