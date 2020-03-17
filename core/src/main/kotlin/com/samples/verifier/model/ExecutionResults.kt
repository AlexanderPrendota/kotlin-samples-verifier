package com.samples.verifier.model

import com.samples.verifier.internal.utils.ErrorDescriptor
import com.samples.verifier.internal.utils.ExceptionDescriptor
import com.samples.verifier.internal.utils.ExecutionResult
import java.lang.Exception

class ExecutionResults(val results: Map<String, ExecutionResult?>, val preExecutionError: Exception? = null) {
    val haveNoErrors = results.all { it.value?.errors?.isEmpty() ?: false }
    val haveNoExceptions = results.all { it.value != null && it.value?.exception == null }
    val errors: Map<String, List<ErrorDescriptor>> = results.map {
        it.value?.errors?.toList()?.getOrNull(0)
    }.filterNotNull().toMap()
    val exceptions: Map<String, ExceptionDescriptor?> = results.mapValues { it.value?.exception }
    val successful = haveNoErrors && haveNoExceptions && (preExecutionError == null)
}