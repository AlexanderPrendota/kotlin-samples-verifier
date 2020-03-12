package com.samples.verifier

import org.eclipse.jgit.transport.URIish

interface SamplesParser {
    var sourceDir: String
    var repositoryURL: URIish?
    var flags: List<String>
    var targetDir: String

    fun processGitRepository()

    fun processDirectory()
}