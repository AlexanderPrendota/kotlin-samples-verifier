package com.samples.verifier.internal.api

import com.samples.verifier.model.ExecutionResult
import com.samples.verifier.model.Project
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface SamplesVerifierService {
    @POST("/api/compiler/run")
    @Headers("Content-Type: application/json")
    fun executeCodeJVM(@Body body: Project): Call<ExecutionResult>
}