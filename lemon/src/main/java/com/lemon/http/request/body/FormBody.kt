package com.lemon.http.request.body

import com.lemon.http.request.ContentType
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.net.URLEncoder

/**
 * Describe:表单请求 Body
 * Created by liya.zhu on 2022/3/7
 */
class FormBody(val encodedNames: List<String>, val encodedValues: List<String>) :
    RequestBody() {

    override fun contentType() = ContentType.FROM

    override fun measureContentLength(): Long {
        return ByteArrayOutputStream().let {
            writeTo(it)
            it.size().toLong()
        }
    }

    override fun writeTo(outputStream: OutputStream) {
        for (i in encodedNames.indices) {
            if (i > 0) outputStream.write("&".toByteArray())
            outputStream.write(encodedNames[i].toByteArray())
            outputStream.write("=".toByteArray())
            outputStream.write(encodedValues[i].toByteArray())
        }
    }

    fun newBuilder() = Builder(encodedNames.toMutableList(), encodedValues.toMutableList())

    class Builder(
        private val encodedNames: MutableList<String> = mutableListOf(),
        private val encodedValues: MutableList<String> = mutableListOf()
    ) {

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