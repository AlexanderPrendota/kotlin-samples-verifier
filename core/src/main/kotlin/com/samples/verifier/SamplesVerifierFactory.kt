package com.samples.verifier

import com.samples.verifier.internal.SamplesVerifierInstance
import com.samples.verifier.model.Config

object SamplesVerifierFactory {
    @JvmStatic
    fun create(config: Config): SamplesVerifier = SamplesVerifierInstance(config)
}