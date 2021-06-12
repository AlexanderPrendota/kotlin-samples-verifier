package com.samples.pusher.core.model

import com.samples.pusher.core.Snippet
import com.samples.verifier.model.CollectionOfRepository

interface TemplateModel {
  val src: CollectionOfRepository
}

data class NewSamplesModel(
  override val src: CollectionOfRepository,
  val changedFiles: List<String>,
  val badSnippets: List<Snippet>
) : TemplateModel

data class BadSamplesModel(
  override val src: CollectionOfRepository,
  val snippets: List<Snippet>
) : TemplateModel