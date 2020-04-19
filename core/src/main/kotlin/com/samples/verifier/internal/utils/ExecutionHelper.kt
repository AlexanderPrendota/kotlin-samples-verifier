package com.samples.verifier.internal.utils

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.samples.verifier.CallException
import com.samples.verifier.Code
import com.samples.verifier.KotlinEnv
import com.samples.verifier.internal.api.SamplesVerifierService
import com.samples.verifier.model.*
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.converter.jaxb.JaxbConverterFactory
import java.io.IOException

const val NUMBER_OF_REQUESTS = 3

internal class ExecutionHelper(baseUrl: String, private val kotlinEnv: KotlinEnv) {

    private val service: SamplesVerifierService = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(JaxbConverterFactory.create())
        .addConverterFactory(JacksonConverterFactory.create(jacksonObjectMapper()))
        .build()
        .create(SamplesVerifierService::class.java)

    fun executeCode(
        code: Code
    ): ExecutionResult {
        return when (kotlinEnv) {
            KotlinEnv.JVM -> executeCodeJVM(KotlinFile("filename.kt", code))
            KotlinEnv.JS -> executeCodeJS(KotlinFile("filename.kt", code))
        }
    }

    private fun executeCodeJVM(kotlinFile: KotlinFile): ExecutionResult {
        val project = Project("", listOf(kotlinFile))
        var response: Response<ExecutionResponse>? = null
        for (i in 1..NUMBER_OF_REQUESTS) {
            try {
                response = service.executeCodeJVM(project).execute()
                if (response!!.isSuccessful) {
                    break
                }
            } catch (e: IOException) {
                if (i == NUMBER_OF_REQUESTS) throw e
            }
        }
        return if (response!!.isSuccessful) {
            val r = response.body()!!
            ExecutionResult(r.errors["filename.kt"] ?: error("unexpected response structure"), r.exception, r.text)
        } else throw CallException(response.errorBody()!!.string())
    }

    private fun executeCodeJS(kotlinFile: KotlinFile): ExecutionResult {
        TODO("Not yet implemented")
    }
}

internal data class ExecutionResponse(
    val errors: Map<String, List<ErrorDescriptor>>,
    val exception: ExceptionDescriptor?,
    val text: String
)