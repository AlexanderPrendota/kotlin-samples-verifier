package com.samples.verifier

import com.samples.verifier.internal.SamplesVerifierInstance

object SamplesVerifierFactory {
    @JvmOverloads
    @JvmStatic
    fun create(
        compilerUrl: String = "http://localhost:8080/",
        compilerType: CompilerType = CompilerType.JVM
    ): SamplesVerifier = SamplesVerifierInstance(compilerUrl, compilerType)
}

enum class CompilerType {
    JVM,
    JS
}