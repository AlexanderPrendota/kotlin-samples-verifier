package com.samples.verifier

import com.samples.verifier.internal.SamplesVerifierInstance

const val DEFAULT_COMPILER_HOST = "http://localhost:8080"

object SamplesVerifierFactory {
    /**
     * @param compilerUrl kotlin-compiler-server url
     * @param kotlinEnv [KotlinEnv]
     */
    @JvmOverloads
    @JvmStatic
    fun create(
        compilerUrl: String = DEFAULT_COMPILER_HOST,
        kotlinEnv: KotlinEnv = KotlinEnv.JVM
    ): SamplesVerifier = SamplesVerifierInstance(compilerUrl, kotlinEnv)
}

enum class KotlinEnv {
    JVM,
    JS
}