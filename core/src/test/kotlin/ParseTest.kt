import com.samples.verifier.CodeSnippet
import com.samples.verifier.FileType
import com.samples.verifier.SamplesVerifierFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ParseTest {
    private val codeSnippetsFromRepo = run {
        val expectedResult = mutableListOf(
            "fun main() {\n    println(\"Hello world!\")\n}"
        )
        for (i in 2..5) {
            with(expectedResult) {
                add("fun main() {\n    println(\"Hello world!$i.1\")\n}")
                add("fun main() {\n    println(\"Hello world!$i.2\")\n}")
            }
        }
        expectedResult
    }

    @Test
    fun `parse with list test`() {
        val samplesVerifier = SamplesVerifierFactory.create()
        val results = listOf(FileType.MD, FileType.HTML).map {
            samplesVerifier.parse(
                "https://github.com/AlexanderPrendota/kotlin-samples-verifier.git",
                "tests",
                listOf("run-kotlin"),
                emptyList(),
                it
            ) { snippets: List<CodeSnippet> ->
                snippets.map { it.code }.sorted()
            }
        }
        val expectedResult = codeSnippetsFromRepo.sorted()
        Assertions.assertEquals(listOf(expectedResult, expectedResult), results)
    }

    @Test
    fun `parse test`() {
        val samplesVerifier = SamplesVerifierFactory.create()
        val results = listOf(FileType.MD, FileType.HTML).map {
            samplesVerifier.parse(
                "https://github.com/AlexanderPrendota/kotlin-samples-verifier.git",
                "tests",
                listOf("run-kotlin"),
                emptyList(),
                it
            ) { codeSnippet: CodeSnippet ->
                codeSnippet.code
            }.toList()
        }
        val expectedResult =
            codeSnippetsFromRepo.sorted().map { it to it }
        Assertions.assertEquals(
            listOf(1, 2).map { expectedResult },
            results.map { it.sortedWith(compareBy{ it.first }) }
        )
    }
}