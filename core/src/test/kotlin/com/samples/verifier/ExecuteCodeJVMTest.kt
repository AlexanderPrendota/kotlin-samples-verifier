package com.samples.verifier

import com.samples.verifier.base.BaseExecuteFileTest
import com.samples.verifier.model.ParseConfiguration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


class ExecuteCodeJVMTest : BaseExecuteFileTest() {
  @BeforeEach
  fun setConfig() {
    defaultConfiguration = ParseConfiguration {
      snippetFlags = hashSetOf("run-kotlin")
    }
  }

  @Test
  fun `base md jvm test`() {
    hasNoErrors("src/test/resources/hello_world.md", FileType.MD)
    containsOutput(
      "src/test/resources/hello_world.md",
      FileType.MD,
      "fun main() {\n    println(\"Hello world!\")\n}",
      "<outStream>Hello world!\n</outStream>"
    )
  }

  @Test
  fun `base html jvm test`() {
    hasNoErrors("src/test/resources/hello_world.html", FileType.HTML)
    containsOutput(
      "src/test/resources/hello_world.html",
      FileType.HTML,
      "fun main() {\n    println(\"Hello world!\")\n}",
      "<outStream>Hello world!\n</outStream>"
    )
  }
}