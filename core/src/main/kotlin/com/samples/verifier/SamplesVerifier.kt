package com.samples.verifier

import com.samples.verifier.internal.Config
import org.eclipse.jgit.transport.URIish

interface SamplesVerifier {
    val config: Config

    fun run()

    fun run(repositoryURL: URIish)

    fun run(sourceDir: String)
}