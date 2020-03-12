package com.samples.verifier

import com.samples.verifier.internal.SamplesVerifierInstance
import com.samples.verifier.internal.Config

object SamplesVerifierFactory {
    @JvmStatic
    fun create(config: Config): SamplesVerifier = SamplesVerifierInstance(config)
}