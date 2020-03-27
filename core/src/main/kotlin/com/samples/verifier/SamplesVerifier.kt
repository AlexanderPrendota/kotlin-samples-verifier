package com.samples.verifier

import com.samples.verifier.model.ExecutionResult

interface SamplesVerifier {
    fun collect(url: String, attributes: List<String>, type: FileType): Map<ExecutionResult, Code>

    fun check(url: String, attributes: List<String>, type: FileType)
}

enum class FileType {
    MD,
    HTML
}

typealias Code = String