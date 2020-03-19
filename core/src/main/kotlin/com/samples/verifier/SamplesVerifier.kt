package com.samples.verifier

import com.samples.verifier.model.Config
import com.samples.verifier.model.ExecutionResults
import org.eclipse.jgit.transport.URIish

interface SamplesVerifier {
    val config: Config

    fun run(): ExecutionResults

    fun run(repositoryURL: URIish): ExecutionResults

    fun run(sourceDir: String): ExecutionResults
}