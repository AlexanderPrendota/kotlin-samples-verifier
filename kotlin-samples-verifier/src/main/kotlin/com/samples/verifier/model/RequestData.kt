package com.samples.verifier.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

data class KotlinFile(val name: String, val text: String)

data class Project(val args: String, val files: List<KotlinFile>)

data class TextInterval(val start: TextPosition, val end: TextPosition) {
  data class TextPosition(val line: Int, val ch: Int)
}

enum class ProjectSeverity {
  INFO,
  WARNING,
  ERROR;
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class ErrorDescriptor(
  val interval: TextInterval,
  val message: String,
  val severity: ProjectSeverity,
  val className: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class StackTraceElement(
  val className: String = "",
  val methodName: String = "",
  val fileName: String = "",
  val lineNumber: Int = 0
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ExecutionResult(
  val errors: List<ErrorDescriptor>,
  val exception: ExceptionDescriptor?,
  val text: String,
  val fileName: String = ""
)