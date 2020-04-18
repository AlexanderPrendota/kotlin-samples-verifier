package com.samples.verifier

import com.samples.verifier.model.ExecutionResult

interface SamplesVerifier {
    /**
     * Collect execution results for code snippets from a git repository
     *
     * @param url git repository url
     * @param attributes list of attributes (classes for HTML or meta-information for MD)
     * @param type [FileType]
     * @return map with results as keys and code as values
     * @throws CallException
     */
    fun collect(url: String, attributes: List<String>, type: FileType): Map<ExecutionResult, Code>

    /**
     * Execute code snippets from a git repository
     *
     * @param url git repository url
     * @param attributes list of attributes (classes for HTML or meta-information for MD)
     * @param type [FileType]
     * @throws CallException
     */
    fun check(url: String, attributes: List<String>, type: FileType)

    /**
     * Parse code snippets from a git repository and process them using [processResult] function.
     *
     * @param url git repository url
     * @param attributes list of attributes (classes for HTML or meta-information for MD)
     * @param type [FileType]
     * @param processResult function to process list of code snippets, should not change list size
     * @throws CallException
     */
    fun <T> parse(
        url: String,
        attributes: List<String>,
        type: FileType,
        processResult: (List<Code>) -> List<T>
    ): Map<T, Code>
}

enum class FileType {
    MD,
    HTML
}

typealias Code = String