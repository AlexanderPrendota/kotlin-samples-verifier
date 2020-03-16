package com.samples.verifier.internal.utils

import com.fasterxml.jackson.annotation.JsonIgnore

data class KotlinFile(val name: String, val text: String)

data class Project(val args: String, val files: List<KotlinFile>)

data class TextInterval(val start: TextPosition, val end: TextPosition) {
    data class TextPosition(val line: Int, val ch: Int)
}

enum class ProjectSeveriry {
    INFO,
    ERROR,
    WARNING;
}

data class ErrorDescriptor(
    val interval: TextInterval,
    val message: String,
    val severity: ProjectSeveriry,
    val className: String? = null
)

data class StackTraceElement(
    val className: String = "",
    val methodName: String = "",
    val fileName: String = "",
    val lineNumber: Int = 0
)

data class ExceptionDescriptor(
    val message: String? = null,
    val fullName: String? = null,
    val stackTrace: List<StackTraceElement> = emptyList(),
    val cause: ExceptionDescriptor? = null,
    val localizedMessage: String? = null
)

data class ExecutionResult(
    val errors: Map<String, List<ErrorDescriptor>> = emptyMap(),
    val exception: ExceptionDescriptor? = null,
    val text: String = "",
    @JsonIgnore var numberOfCalls: Int = 1
)