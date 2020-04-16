import com.samples.verifier.KotlinEnv
import com.samples.verifier.internal.utils.ExecutionHelper
import com.samples.verifier.model.ExecutionResult
import com.samples.verifier.model.KotlinFile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.FileInputStream


class ExecuteJSTest {

    @Test
    fun `base KotlinJs test`() {
        val executionHelper = ExecutionHelper("http://localhost:8080/", KotlinEnv.JS)
        lateinit var result: Pair<ExecutionResult, KotlinFile>
        val code = """
                    fun main() {
                        println("Hello world!")
                    }""".trimMargin()
        executionHelper.executeCode(KotlinFile("hello_world.kt", code)) { res, file ->
            result = res to file
        }
        //TODO why "/n" before output?..
        val expectedResult = ExecutionResult(
            mapOf("hello_world.kt" to emptyList()),
            null, "<outStream>\nHello world!\n</outStream>"
        )
        assertEquals(expectedResult, result.first)
        assertEquals(code, result.second.text)
    }

    @Test
    fun `base KotlinJs test 2`() {
        val executionHelper = ExecutionHelper("http://localhost:8080/", KotlinEnv.JS)
        lateinit var result: Pair<ExecutionResult, KotlinFile>
        val code = String(FileInputStream("src/test/resources/base_kotlin_js_test_2.txt").readAllBytes())
        executionHelper.executeCode(KotlinFile("test.kt", code)) { res, file ->
            result = res to file
        }
        //TODO why "/n" before output?..
        val expectedResult = ExecutionResult(
            mapOf("test.kt" to emptyList()),
            null, "<outStream>\n" +
                    "a == a\n" +
                    "97 == 97\n" +
                    "bbb\n" +
                    "2 + 2 = 4\n" +
                    "'2' + 2 = 22\n</outStream>"
        )
        assertEquals(expectedResult, result.first)
        assertEquals(code, result.second.text)
    }

    @Disabled("not supported yet")
    @Test
    fun `test with alert`() {
        val executionHelper = ExecutionHelper("http://localhost:8080/", KotlinEnv.JS)
        lateinit var result: Pair<ExecutionResult, KotlinFile>
        val code = """
                    external fun alert(msg: String)   // 1

                    fun main() {
                      alert("Hi!")                    // 2
                    }""".trimMargin()
        executionHelper.executeCode(KotlinFile("test.kt", code)) { res, file ->
            result = res to file
        }
        val expectedResult = TODO()
        assertEquals(expectedResult, result.first)
        assertEquals(code, result.second.text)
    }
}