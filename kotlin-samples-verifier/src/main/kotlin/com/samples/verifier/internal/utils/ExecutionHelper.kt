package com.samples.verifier.internal.utils

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.samples.verifier.CallException
import com.samples.verifier.CodeSnippet
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
    codeSnippet: CodeSnippet
  ): ExecutionResult {
    return when (kotlinEnv) {
      KotlinEnv.JVM -> executeCodeJVM(codeSnippet)
      KotlinEnv.JS -> executeCodeJS(codeSnippet)
    }
  }

  private fun executeCodeJVM(codeSnippet: CodeSnippet): ExecutionResult {
    val project = Project("", listOf(KotlinFile("filename.kt", codeSnippet.code)))
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
      ExecutionResult(
        r.errors["filename.kt"] ?: error("unexpected response structure"),
        r.exception,
        r.text,
        codeSnippet.filename
      )
    } else throw CallException(response.errorBody()!!.string())
  }

  private fun executeCodeJS(@Suppress("UNUSED_PARAMETER") codeSnippet: CodeSnippet): ExecutionResult {
    TODO("Not yet implemented")
  }
}

internal data class ExecutionResponse(
  val errors: Map<String, List<ErrorDescriptor>>,
  val exception: ExceptionDescriptor?,
  val text: String
)