package com.samples.verifier.model

import com.samples.verifier.Code

data class CollectionOfRepository (
        var url: String,
        var branch: String,
        var snippets : Map<Code, ExecutionResult>,
        var diff: DiffOfRepository? = null
)

data class DiffOfRepository (
        var startCommit: String,
        var endCommit: String,
        var deletedFiles : List<String>
)