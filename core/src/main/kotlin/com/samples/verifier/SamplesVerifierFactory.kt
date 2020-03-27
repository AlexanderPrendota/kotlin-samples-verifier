package com.samples.verifier

import com.samples.verifier.internal.SamplesVerifierInstance

object SamplesVerifierFactory {
    @JvmOverloads
    @JvmStatic
    fun create(
        compilerUrl: String = "http://localhost:8080/",
        kotlinEnv: KotlinEnv = KotlinEnv.JVM
    ): SamplesVerifier = SamplesVerifierInstance(compilerUrl, kotlinEnv)
}

enum class KotlinEnv {
    JVM,
    JS
}