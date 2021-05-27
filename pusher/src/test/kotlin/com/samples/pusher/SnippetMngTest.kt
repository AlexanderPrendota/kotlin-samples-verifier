package com.samples.pusher

import com.samples.pusher.core.SnippetManager
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.io.path.createTempDirectory

class SnippetMngTest {
  @Test
  fun `adding new samples`() {
    val tempDir = createTempDirectory("test").toFile()
    val snippetMng = SnippetManager(tempDir)

    snippetMng.addSnippet("code1", "samples/test1.md")
    snippetMng.addSnippet("code2", "samples/test1.md")

    Assertions.assertEquals("test1", tempDir.list()[0])
    Assertions.assertEquals(2, tempDir.resolve("test1").list().size)

    snippetMng.addSnippet("code3", "samples/test2.md")
    snippetMng.addSnippet("code4", "samples/test3.md")
    Assertions.assertEquals(3, tempDir.list().size)

    snippetMng.removeAllSnippets("samples/test3.md")
    Assertions.assertEquals(2, tempDir.list().size)
  }
}