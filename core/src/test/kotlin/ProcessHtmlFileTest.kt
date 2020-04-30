import com.samples.verifier.internal.utils.processHTMLFile
import com.samples.verifier.model.Attribute
import com.samples.verifier.model.ParseConfiguration
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File

class ProcessHtmlFileTest {
  @Test
  fun `process html test`() {
    val helloWorld = """
      fun main() {
          println("Hello world!")
      }
    """.trimIndent()

    val res = processHTMLFile(
      File("src/test/resources/html_test.html"),
      ParseConfiguration {
        snippetFlags = hashSetOf("kotlin", "run-kotlin")
        ignoreAttributes = hashSetOf(
          Attribute("data-highlight-only", ""),
          Attribute("another-ignore-attribute", "ignore")
        )
        parseTags = hashSetOf("code", "div")
      })
    Assertions.assertEquals(listOf(1, 2, 3, 4, 5).map { helloWorld }, res)
  }
}