package com.samples.verifier.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

data class ExceptionDescriptor(
    val message: String?,
    val fullName: String?,
    val stackTrace: List<StackTraceElement>,
    val cause: ExceptionDescriptor?,
    val localizedMessage: String?
)

fun Throwable.toExceptionDescriptor(): ExceptionDescriptor {
    val mapper = jacksonObjectMapper()
    val rawException = mapper.writeValueAsString(this)
    return mapper.readValue(rawException, ExceptionDescriptor::class.java)
}
