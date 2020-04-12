package com.samples.verifier.internal.utils

import com.samples.verifier.internal.components.ErrorStream
import com.samples.verifier.internal.components.OutStream
import com.samples.verifier.model.ExecutionResult
import com.samples.verifier.model.TranslationJSResult
import com.samples.verifier.model.toExceptionDescriptor
import org.graalvm.polyglot.Context
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.PrintStream

internal fun executeJS(translationJSResult: TranslationJSResult): ExecutionResult {
    val outputStream = ByteArrayOutputStream()
    val capturedErr = ErrorStream(outputStream)
    val capturedOutput = OutStream(outputStream)
    val translationResult = captureOutErr(capturedOutput, capturedErr) {
        //TODO alert is not defined
        Context.newBuilder("js").allowIO(true).build().use {
            it.eval("js", "load('core/build/kotlin-js/kotlin.js');")
            try {
                it.eval("js", translationJSResult.jsCode)
            } catch (e: Exception) {
                translationJSResult.exception = e.toExceptionDescriptor()
            }
            translationJSResult
        }
    }
    val text = outputStream.toString()
        .replace("</errStream><errStream>".toRegex(), "")
        .replace("</outStream><outStream>".toRegex(), "")
    return translationResult.let { ExecutionResult(it.errors, it.exception, text) }
}

internal fun <T> captureOutErr(newOut: OutputStream, newErr: OutputStream, block: () -> T): T {
    val systemOut = System.out
    val systemErr = System.err
    val strOutput = PrintStream(newOut)
    val strErr = PrintStream(newErr)
    System.setOut(strOutput)
    System.setErr(strErr)

    val result = block()

    System.out.flush()
    System.err.flush()
    System.setOut(systemOut)
    System.setErr(systemErr)
    return result
}