package com.samples.verifier.model

import com.samples.verifier.CodeSnippet

data class CollectionOfRepository(
  var url: String,
  var branch: String,
  var snippets: Map<CodeSnippet, ExecutionResult>,
  var diff: DiffOfRepository? = null
)

data class DiffOfRepository(
  var startRef: String,
  var endRef: String,
  var deletedFiles: List<String>
)