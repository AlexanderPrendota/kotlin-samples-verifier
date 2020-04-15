package com.samples.verifier.internal.components

import java.io.IOException
import java.io.OutputStream


internal class ErrorStream(private val outputStream: OutputStream) : OutputStream() {

    @Throws(IOException::class)
    override fun write(b: Int) {
        outputStream.write("<errStream>".toByteArray())
        outputStream.write(b)
        outputStream.write("</errStream>".toByteArray())
    }

    @Throws(IOException::class)
    override fun write(b: ByteArray) {
        outputStream.write("<errStream>".toByteArray())
        outputStream.write(b)
        outputStream.write("</errStream>".toByteArray())
    }

    @Throws(IOException::class)
    override fun write(b: ByteArray, offset: Int, length: Int) {
        outputStream.write("<errStream>".toByteArray())
        outputStream.write(b, offset, length)
        outputStream.write("</errStream>".toByteArray())
    }
}

internal class OutStream(private val outputStream: OutputStream) : OutputStream() {

    @Throws(IOException::class)
    override fun write(b: Int) {
        outputStream.write("<outStream>".toByteArray())
        outputStream.write(b)
        outputStream.write("</outStream>".toByteArray())
    }

    @Throws(IOException::class)
    override fun write(b: ByteArray) {
        outputStream.write("<outStream>".toByteArray())
        outputStream.write(b)
        outputStream.write("</outStream>".toByteArray())
    }

    @Throws(IOException::class)
    override fun write(b: ByteArray, offset: Int, length: Int) {
        outputStream.write("<outStream>".toByteArray())
        outputStream.write(b, offset, length)
        outputStream.write("</outStream>".toByteArray())
    }
}
