import com.samples.verifier.Code
import com.samples.verifier.FileType
import com.samples.verifier.KotlinEnv
import com.samples.verifier.internal.utils.ExecutionHelper
import com.samples.verifier.internal.utils.processFile
import com.samples.verifier.model.ExecutionResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File


class ExecuteCodeJVMTest {
    @Test
    fun `base md jvm test`() {
        val result = withExecutionHelper(
            File("src/test/resources/hello_world.md"),
            FileType.MD
        ) {
            executeCode(it)
        }
        val expectedResult = listOf(ExecutionResult(
            emptyList(),
            null, "<outStream>Hello world!\n</outStream>"
        ) to "fun main() {\n    println(\"Hello world!\")\n}")
        assertEquals(expectedResult, result)
    }

    @Test
    fun `base html jvm test`() {
        val result = withExecutionHelper(
            File("src/test/resources/hello_world.html"),
            FileType.HTML
        ) {
            executeCode(it)
        }
        val expectedResult = listOf(ExecutionResult(
            emptyList(),
            null, "<outStream>Hello world!\n</outStream>"
        ) to "fun main() {\n    println(\"Hello world!\")\n}")
        assertEquals(expectedResult, result)
    }

    private fun <T> withExecutionHelper(
        file: File,
        fileType: FileType,
        block: ExecutionHelper.(Code) -> T
    ): List<Pair<T, Code>> {
        val executionHelper = ExecutionHelper("http://localhost:8080/", KotlinEnv.JVM)
        val result = mutableListOf<Pair<T, Code>>()
        val snippets = processFile(
            file,
            fileType,
            listOf("run-kotlin")
        )
        for (code in snippets) {
            val res = executionHelper.block(code)
            result.add(res to code)
        }
        return result
    }
}