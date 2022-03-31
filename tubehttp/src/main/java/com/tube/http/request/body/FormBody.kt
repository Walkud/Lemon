package com.tube.http.request.body

import com.tube.http.request.ContentType
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.net.URLEncoder

/**
 * Describe:
 * Created by liya.zhu on 2022/3/7
 */
class FormBody(private val encodedNames: List<String>, private val encodedValues: List<String>) :
    RequestBody() {

    override fun contentType() = ContentType.FROM

    override fun contentLength(): Long {
        val byteOpt = ByteArrayOutputStream()
        writeTo(byteOpt)
        return byteOpt.size().toLong()
    }

    override fun writeTo(outputStream: OutputStream) {
        for (i in encodedNames.indices) {
            if (i > 0) outputStream.write("&".toByteArray())
            outputStream.write(encodedNames[i].toByteArray())
            outputStream.write("=".toByteArray())
            outputStream.write(encodedValues[i].toByteArray())
        }
    }

    class Builder {

        private val encodedNames = mutableListOf<String>()
        private val encodedValues = mutableListOf<String>()

        fun add(name: String, value: String, encode: Boolean = false) {
            encodedNames.add(name)

            val encodedValue = if (encode) {
                URLEncoder.encode(value, ContentType.FROM.getCharset().name())
            } else {
                value
            }
            encodedValues.add(encodedValue)
        }

        fun build() = FormBody(encodedNames, encodedValues)

    }

}