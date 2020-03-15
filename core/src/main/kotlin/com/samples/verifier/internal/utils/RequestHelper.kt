package com.samples.verifier.internal.utils

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.samples.verifier.internal.api.SamplesVerifierService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.converter.jaxb.JaxbConverterFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

const val NUMBER_OF_REQUESTS = 5

internal class RequestHelper(private val baseUrl: String) {
    private val calls = ConcurrentLinkedQueue<Call<ExecutionResult>>()
    private val _responses = ConcurrentHashMap<String, Response<ExecutionResult>>()
    private val errors = ConcurrentHashMap<String, Throwable>()
    private val finished = AtomicInteger()

    private val service: SamplesVerifierService = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(JaxbConverterFactory.create())
        .addConverterFactory(JacksonConverterFactory.create(jacksonObjectMapper()))
        .build()
        .create(SamplesVerifierService::class.java)

    val responses: Map<String, Response<ExecutionResult>>
        get() {
            while (finished.get() != calls.size) {
                if (Thread.interrupted()) {
                    for (call in calls) {
                        call.cancel()
                    }
                    throw InterruptedException()
                }
                try {
                    Thread.sleep(100)
                } catch (ie: InterruptedException) {
                    for (call in calls) {
                        call.cancel()
                    }
                    throw ie
                }
            }

            if (Thread.interrupted()) {
                throw InterruptedException()
            }

            if (errors.isNotEmpty()) {
                val exception = Exception()
                for (error in errors.values) {
                    exception.addSuppressed(error)
                }
                throw exception
            }
            return _responses
        }


    fun compileCodeRequest(project: Project, filename: String) {
        val call = service.compileCode(project)
        val cnt = AtomicInteger(0)
        call.enqueue(object : Callback<ExecutionResult> {
            override fun onFailure(call: Call<ExecutionResult>, t: Throwable) {
                try {
                    errors[filename] = t
                } finally {
                    finished.incrementAndGet()
                }
            }

            override fun onResponse(call: Call<ExecutionResult>, response: Response<ExecutionResult>) {
                try {
                    if (response.body() != null && response.body()!!.exception != null && cnt.getAndIncrement() < NUMBER_OF_REQUESTS) {
                        val retry = call.clone()
                        retry.enqueue(this)
                        calls.add(call)
                    } else {
                        _responses[filename] = response
                    }
                } finally {
                    finished.incrementAndGet()
                }
            }

        })
        calls.add(call)
    }
}