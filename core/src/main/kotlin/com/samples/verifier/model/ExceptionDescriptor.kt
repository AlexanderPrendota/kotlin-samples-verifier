package com.samples.verifier.model

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import java.io.IOException

data class ExceptionDescriptor(
    val message: String? = null,
    val fullName: String? = null,
    val stackTrace: List<StackTraceElement> = emptyList(),
    val cause: ExceptionDescriptor? = null,
    val localizedMessage: String? = null
)

class ThrowableSerializer : JsonSerializer<Throwable>() {
    @Throws(IOException::class)
    override fun serialize(value: Throwable, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeStartObject()
        gen.writeStringField("message", value.message)
        gen.writeStringField("fullName", value.javaClass.name)
        gen.writeObjectField("stackTrace", value.stackTrace?.take(3))
        gen.writeObjectField("cause", if (value.cause != value) value.cause else null)
        gen.writeEndObject()
    }
}

val outputMapper = ObjectMapper().apply {
    registerModule(SimpleModule().apply {
        addSerializer(Throwable::class.java, ThrowableSerializer())
    })
}

fun Throwable.toExceptionDescriptor(): ExceptionDescriptor {
    val rawException = outputMapper.writeValueAsString(this)
    return outputMapper.readValue(rawException, ExceptionDescriptor::class.java)
}
