package com.samples.verifier.base

import com.samples.verifier.DEFAULT_COMPILER_HOST
import com.samples.verifier.FileType
import com.samples.verifier.KotlinEnv
import com.samples.verifier.SamplesVerifierFactory
import com.samples.verifier.model.ProjectSeveriry
import org.junit.jupiter.api.Assertions.assertTrue

open class BaseSamplesVerifierTest(host: String = DEFAULT_COMPILER_HOST, kotlinEnv: KotlinEnv = KotlinEnv.JVM) {
  val samplesVerifier = SamplesVerifierFactory.create(host, kotlinEnv)

  fun assertNoErrors(url: String, branch: String, type: FileType) {
    val results = samplesVerifier.collect(url, branch, type).snippets
    assertTrue(results.all { it.value.errors.all { it.severity != ProjectSeveriry.ERROR } })
  }
}