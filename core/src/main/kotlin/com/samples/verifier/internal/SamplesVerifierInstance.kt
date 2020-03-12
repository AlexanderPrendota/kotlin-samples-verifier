package com.samples.verifier.internal

import com.samples.verifier.SamplesVerifier
import org.eclipse.jgit.transport.URIish

class SamplesVerifierInstance(override val config: Config) : SamplesVerifier {
    override fun run(repositoryURL: URIish) {
        TODO("Not yet implemented")
    }

    override fun run(sourceDir: String) {
        TODO("Not yet implemented")
    }
}