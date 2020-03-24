package com.samples.verifier.model

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
    val className: String?
)

data class StackTraceElement(
    val className: String,
    val methodName: String,
    val fileName: String,
    val lineNumber: Int
)

data class ExceptionDescriptor(
    val message: String?,
    val fullName: String?,
    val stackTrace: List<StackTraceElement>,
    val cause: ExceptionDescriptor?,
    val localizedMessage: String?
)

data class ExecutionResult(
    val errors: Map<String, List<ErrorDescriptor>>,
    val exception: ExceptionDescriptor?,
    val text: String
)