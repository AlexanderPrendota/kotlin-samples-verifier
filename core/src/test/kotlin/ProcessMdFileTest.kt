import com.samples.verifier.FileType
import com.samples.verifier.internal.utils.processFile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class ProcessMdFileTest {
  @Test
  fun `process md test`() {
    val helloWorld = """
      fun main() {
          println("Hello world!")
      }
    """.trimIndent()
    val res = processFile(
      File("src/test/resources/md_test.md"),
      FileType.MD,
      listOf("kotlin", "run-kotlin"),
      listOf("data-highlight-only")
    )
    assertEquals(listOf(1, 2, 3, 4, 5).map { helloWorld }, res)
  }
}