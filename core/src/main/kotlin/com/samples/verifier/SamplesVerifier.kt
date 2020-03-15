package com.samples.verifier

import com.samples.verifier.internal.Config
import com.samples.verifier.internal.utils.ExecutionResult
import org.eclipse.jgit.transport.URIish

interface SamplesVerifier {
    val config: Config

    fun run(): Map<String, ExecutionResult?>

    fun run(repositoryURL: URIish): Map<String, ExecutionResult?>

    fun run(sourceDir: String): Map<String, ExecutionResult?>
}