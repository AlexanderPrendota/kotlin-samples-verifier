package com.samples.verifier.internal.utils

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.samples.verifier.internal.api.SamplesVerifierService
import com.samples.verifier.model.ExecutionResults
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.converter.jaxb.JaxbConverterFactory
import java.util.concurrent.ConcurrentHashMap

const val NUMBER_OF_REQUESTS = 5

internal class RequestHelper(private val baseUrl: String) {
    private val requests = hashMapOf<String, Deferred<ExecutionResult>>()
    private val _responses = ConcurrentHashMap<String, ExecutionResult>()

    private val service: SamplesVerifierService = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(JaxbConverterFactory.create())
        .addConverterFactory(JacksonConverterFactory.create(jacksonObjectMapper()))
        .build()
        .create(SamplesVerifierService::class.java)

    val responses: ExecutionResults
        get() {
            runBlocking {
                requests.map {
                    val m = it.value.await()
                    _responses[it.key] = m
                }
            }
            return ExecutionResults(_responses)
        }

    fun compileCodeRequest(project: Project, filename: String) {
        requests[filename]?.cancel()

        requests[filename] = GlobalScope.async {
            var result = service.compileCode(project)
            for (i in 1..NUMBER_OF_REQUESTS) {
                if (result.exception == null) break
                result = service.compileCode(project)
            }
            result
        }
    }
}