package com.samples.verifier

import com.samples.verifier.model.ExecutionResult

interface SamplesVerifier {
    /**
     * Collect execution results for code snippets from a git repository
     *
     * @param url git repository url
     * @param branch can be specified as ref name (refs/heads/master),
     *               branch name (master) or tag name (v1.2.3).
     * @param attributes list of attributes (classes for HTML or meta-information for MD)
     * @param type [FileType]
     * @return map with code as keys and results as values
     * @throws CallException
     */
    fun collect(url: String, branch: String, attributes: List<String>, type: FileType): Map<Code, ExecutionResult>

    /**
     * Execute code snippets from a git repository
     *
     * @param url git repository url
     * @param branch can be specified as ref name (refs/heads/master),
     *               branch name (master) or tag name (v1.2.3).
     * @param attributes list of attributes (classes for HTML or meta-information for MD)
     * @param type [FileType]
     * @throws CallException
     */
    fun check(url: String, branch: String, attributes: List<String>, type: FileType)

    /**
     * Parse code snippets from a git repository and process them using [processResult] function.
     *
     * @param url git repository url
     * @param branch can be specified as ref name (refs/heads/master),
     *               branch name (master) or tag name (v1.2.3).
     * @param attributes list of attributes (classes for HTML or meta-information for MD)
     * @param type [FileType]
     * @param processResult function to process snippet of code
     * @return map with code snippets as keys and results from [processResult] as values
     * @throws CallException
     */
    fun <T> parse(
        url: String,
        branch: String,
        attributes: List<String>,
        type: FileType,
        processResult: (Code) -> T
    ): Map<Code, T>

    /**
     * Parse code snippets from a git repository and process them using [processResult] function.
     *
     * @param url git repository url
     * @param branch can be specified as ref name (refs/heads/master),
     *               branch name (master) or tag name (v1.2.3).
     * @param attributes list of attributes (classes for HTML or meta-information for MD)
     * @param type [FileType]
     * @param processResult takes a list of all code snippets from repository grouped by their source files
     * @return result of [processResult]
     * @throws CallException
     */
    fun <T> parse(
        url: String,
        branch: String,
        attributes: List<String>,
        type: FileType,
        processResult: (List<List<Code>>) -> T
    ): T
}

enum class FileType {
    MD,
    HTML
}

typealias Code = String