import com.samples.verifier.Code
import com.samples.verifier.FileType
import com.samples.verifier.KotlinEnv
import com.samples.verifier.internal.utils.ExecutionHelper
import com.samples.verifier.internal.utils.processFile
import com.samples.verifier.model.ExecutionResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File


class ParseFileTest {

    @Test
    fun `base md jvm test`() {
        val executionHelper = ExecutionHelper("http://localhost:8080/", KotlinEnv.JVM)
        lateinit var result: Pair<ExecutionResult, Code>
        processFile(
            File("src/test/resources/hello_world.md"),
            FileType.MD,
            listOf("run-kotlin")
        ) { snippets ->
            snippets.map {
                val res = executionHelper.executeCode(it)
                result = res to it
            }
        }
        val expectedCode = "fun main() {\n    println(\"Hello world!\")\n}"
        val expectedResult = ExecutionResult(
            mapOf("filename.kt" to emptyList()),
            null, "<outStream>Hello world!\n</outStream>"
        )
        assertEquals(expectedResult, result.first)
        assertEquals(expectedCode, result.second)
    }

    @Test
    fun `base html jvm test`() {
        val executionHelper = ExecutionHelper("http://localhost:8080/", KotlinEnv.JVM)
        lateinit var result: Pair<ExecutionResult, Code>
        processFile(
            File("src/test/resources/hello_world.html"),
            FileType.HTML,
            listOf("run-kotlin")
        ) { snippets ->
            snippets.map {
                val res = executionHelper.executeCode(it)
                result = res to it
            }
        }
        val expectedCode = "fun main() {\n    println(\"Hello world!\")\n}"
        val expectedResult = ExecutionResult(
            mapOf("filename.kt" to emptyList()),
            null, "<outStream>Hello world!\n</outStream>"
        )
        assertEquals(expectedResult, result.first)
        assertEquals(expectedCode, result.second)
    }
}