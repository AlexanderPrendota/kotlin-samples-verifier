package com.samples.verifier.internal.api

import com.samples.verifier.internal.utils.ExecutionResponse
import com.samples.verifier.model.Project
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

internal interface SamplesVerifierService {
  @POST("/api/compiler/run")
  @Headers("Content-Type: application/json")
  fun executeCodeJVM(@Body body: Project): Call<ExecutionResponse>
}