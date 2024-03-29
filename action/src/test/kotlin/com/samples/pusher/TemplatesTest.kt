package com.samples.pusher

import com.samples.pusher.core.Snippet
import com.samples.pusher.core.TemplateManager
import com.samples.pusher.core.TemplateType
import com.samples.verifier.model.CollectionOfRepository
import com.samples.verifier.model.DiffOfRepository
import com.samples.verifier.model.ExecutionResult
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

class TemplatesTest {
  @Test
  fun `templates pr from commit test`() {

    val templates = TemplateManager()
    //templates.configureTemplate("https://raw.githubusercontent.com/vmishenev/kotlin-web-site/master/.github/workflows/")
    templates.configureTemplate("src/test/resources/templates")

    val res = CollectionOfRepository(
      "https://github.com/AlexanderPrendota/kotlin-web-site", "",
      emptyMap(), DiffOfRepository("", "dsf", listOf())
    )

    val model = HashMap<String, Any>()
    model["src"] = res
    model["badSnippets"] = listOf(Snippet("dfd", ExecutionResult(listOf(), null, "kllkgdfg.md")))
    model["snippets"] = listOf(Snippet("dfd", ExecutionResult(listOf(), null, "kllkgdfg.md")))
    model["changedFiles"] = listOf("README.md")

    val temp = templates.getTemplate(TemplateType.PR, model)
    Assertions.assertEquals("New samples from kotlin-web-site", temp.head)
  }
}