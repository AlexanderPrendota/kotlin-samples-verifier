package com.samples.verifier.internal.api

import com.samples.verifier.internal.utils.ExecutionResult
import com.samples.verifier.internal.utils.Project
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface SamplesVerifierService {
    @POST("/api/compiler/run")
    @Headers("Content-Type: application/json")
    suspend fun compileCode(@Body body: Project): ExecutionResult
}