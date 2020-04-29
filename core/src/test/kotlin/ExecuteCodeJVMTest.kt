import com.samples.verifier.Code
import com.samples.verifier.CodeSnippet
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
    val expectedResult = mapOf(
      "fun main() {\n    println(\"Hello world!\")\n}" to
        ExecutionResult(
          emptyList(),
          null, "<outStream>Hello world!\n</outStream>",
          "hello_world"
        )
    )
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
    val expectedResult = mapOf(
      "fun main() {\n    println(\"Hello world!\")\n}" to
        ExecutionResult(
          emptyList(),
          null, "<outStream>Hello world!\n</outStream>",
          "hello_world"
        )
    )
    assertEquals(expectedResult, result)
  }

  private fun <T> withExecutionHelper(
    file: File,
    fileType: FileType,
    block: ExecutionHelper.(CodeSnippet) -> T
  ): Map<Code, T> {
    val executionHelper = ExecutionHelper("http://localhost:8080/", KotlinEnv.JVM)
    val snippets = processFile(
      file,
      fileType,
      listOf("run-kotlin")
    )
    return snippets.associateWith { executionHelper.block(CodeSnippet(file.nameWithoutExtension, it)) }
  }
}