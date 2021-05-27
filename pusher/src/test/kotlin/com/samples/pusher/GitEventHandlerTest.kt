package com.samples.pusher

import com.samples.pusher.client.CheckOptions
import com.samples.pusher.client.handler.EventType
import com.samples.pusher.client.handler.GitEventHandler
import com.samples.pusher.core.SamplesPusher
import com.samples.verifier.Code
import com.samples.verifier.FileType
import com.samples.verifier.SamplesVerifier
import com.samples.verifier.model.CollectionOfRepository
import com.samples.verifier.model.DiffOfRepository
import com.samples.verifier.model.ExecutionResult
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.io.File
import java.util.*


class GitEventHandlerTest {

  private lateinit var pusher: SamplesPusher
  private lateinit var verifier: SamplesVerifier

  @BeforeEach
  fun prepare() {
    val res = CollectionOfRepository(
      "url", "",
      HashMap<Code, ExecutionResult>(), DiffOfRepository("", "dsf", listOf())
    )
    pusher = mock {
      on { push(eq(res), any()) }.thenReturn(true)
      on { filterBadSnippets(any()) } doReturn listOf()
    }

    verifier = mock {
      on {
        collect(
          baseUrl = any(), baseBranch = any(), headUrl = any(),
          headBranch = any(), type = any()
        )
      } doReturn res
      on {
        collect(
          url = any(), branch = any(), type = any(),
          startCommit = any(), endCommit = any()
        )
      } doReturn res
    }
  }

  @Test
  fun `pull request`() {
    val handler = GitEventHandler(verifier, pusher, CheckOptions())
    val res = handler.process(EventType.pull_request, File("src/test/resources/pr-event.json").readText())
    inOrder(verifier, pusher) {
      verify(verifier).collect(any(), any(), any(), any(), any<FileType>())
      verify(pusher).filterBadSnippets(any())
    }
    Assertions.assertTrue(res)
  }

  @Test
  fun `push event`() {
    val handler = GitEventHandler(verifier, pusher, CheckOptions())
    val res = handler.process(EventType.push, File("src/test/resources/push-event.json").readText())
    inOrder(verifier, pusher) {
      verify(verifier).collect(
        url = any(), branch = any(), type = any(),
        startCommit = any(), endCommit = any()
      )
      verify(pusher).push(any(), any())
    }
    Assertions.assertTrue(res)
  }
}