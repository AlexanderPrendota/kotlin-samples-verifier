package com.samples.verifier.internal.utils

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.samples.verifier.CallException
import com.samples.verifier.Code
import com.samples.verifier.KotlinEnv
import com.samples.verifier.internal.api.SamplesVerifierService
import com.samples.verifier.model.*
import org.slf4j.Logger
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.converter.jaxb.JaxbConverterFactory
import java.io.IOException

const val NUMBER_OF_REQUESTS = 3

internal class ExecutionHelper(baseUrl: String, private val kotlinEnv: KotlinEnv, private val logger: Logger) {
    val results = hashMapOf<ExecutionResult, Code>()

    private val service: SamplesVerifierService = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(JaxbConverterFactory.create())
        .addConverterFactory(JacksonConverterFactory.create(jacksonObjectMapper()))
        .build()
        .create(SamplesVerifierService::class.java)

    fun executeCode(kotlinFile: KotlinFile) {
        val result = when (kotlinEnv) {
            KotlinEnv.JVM -> executeCodeJVM(kotlinFile)
            KotlinEnv.JS -> executeCodeJS(kotlinFile)
        }
        val errors = result.errors.values.flatten()
        if (errors.isNotEmpty()) {
            logger.info("Code: \n${kotlinFile.text}")
            logger.info("Errors: \n${errors.joinToString("\n")}")
            result.exception?.let { logger.info("Exception: \n${it.localizedMessage}") }
                ?: logger.info("Output: \n${result.text}")
        } else if (result.exception != null) {
            logger.info("Code: \n${kotlinFile.text}")
            logger.info("Exception: \n${result.exception.message}")
        }
        results[result] = kotlinFile.text
    }

    private fun executeCodeJVM(kotlinFile: KotlinFile): ExecutionResult {
        val project = Project("", listOf(kotlinFile))
        var result: Response<ExecutionResult>? = null
        for (i in 1..NUMBER_OF_REQUESTS) {
            try {
                result = service.executeCodeJVM(project).execute()
                if (result!!.isSuccessful) {
                    break
                }
            } catch (e: IOException) {
                if (i == NUMBER_OF_REQUESTS) throw e
            }
        }
        return if (result!!.isSuccessful) result.body()!!
        else throw CallException(result.errorBody()!!.string())
    }

    private fun executeCodeJS(kotlinFile: KotlinFile): ExecutionResult {
        TODO("Not yet implemented")
    }
}