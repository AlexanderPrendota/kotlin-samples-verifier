import com.samples.verifier.internal.utils.processMarkdownFile
import com.samples.verifier.model.Attribute
import com.samples.verifier.model.ParseConfiguration
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
    val res = processMarkdownFile(
      File("src/test/resources/md_test.md"),
      ParseConfiguration {
        snippetFlags = hashSetOf("kotlin", "run-kotlin")
        ignoreAttributes = hashSetOf(Attribute("data-highlight-only", ""))
        parseTags = hashSetOf("code", "div")
      })
    assertEquals(listOf(1, 2, 3, 4, 5, 6).map { helloWorld }, res)
  }
}