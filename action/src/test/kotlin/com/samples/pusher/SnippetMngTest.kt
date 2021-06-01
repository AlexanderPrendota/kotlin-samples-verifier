package com.samples.pusher

import com.samples.pusher.core.SnippetManager
import com.samples.verifier.KotlinEnv
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.io.path.createTempDirectory

class SnippetMngTest {
  @Test
  fun `adding new samples`() {
    val tempDir = createTempDirectory("test").toFile()
    val snippetMng = SnippetManager(tempDir, kotlinEnv = KotlinEnv.JVM)

    snippetMng.addSnippet("code1", "samples/test1.md")
    snippetMng.addSnippet("code2", "samples/test1.md")



    Assertions.assertEquals("test1", tempDir.list()[0])
    Assertions.assertEquals(2, tempDir.resolve("test1").list().size)

    //check ext
    Assertions.assertEquals("kt", tempDir.resolve("test1").listFiles()[0].extension )

    snippetMng.addSnippet("code3", "samples/test2.md")
    val filename = snippetMng.addSnippet("code4", "samples/test3.md")

    Assertions.assertEquals("samples/test3.md", snippetMng.translateFilenameToAddedSnippetPath(filename))

    Assertions.assertEquals(3, tempDir.list().size)

    snippetMng.removeAllSnippets("samples/test3.md")
    Assertions.assertEquals(2, tempDir.list().size)
  }
}