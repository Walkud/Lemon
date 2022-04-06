package com.tube.http.request.body

import com.tube.http.request.ContentType
import java.io.File
import java.io.OutputStream
import java.util.*

/**
 * Describe:多部件请求 Body
 * Created by liya.zhu on 2022/4/6
 */
class MultipartBody private constructor(
    val boundary: String,
    val parts: List<Part>
) :
    RequestBody() {

    companion object {
        private val CRLF = "\r\n".toByteArray()
        private val DASH = "--".toByteArray()
    }

    override fun contentType() = ContentType.MULTIPART_FORM_DATA

    override fun writeTo(outputStream: OutputStream) {

        for (part in parts) {
            val body = part.requestBody

            outputStream.write(DASH)
            outputStream.write(boundary.toByteArray())
            outputStream.write(CRLF)

            val fileNamePart = part.fileName?.let { fileName ->
                ";filename=\"$fileName\""
            } ?: ""

            val disposition = "Content-Disposition:form-data; name=${part.name}$fileNamePart"
            outputStream.write(disposition.toByteArray())
            outputStream.write(CRLF)

            part.encoding?.let { encoding ->
                val transferEncoding = "Content-Transfer-Encoding:$encoding;"
                outputStream.write(transferEncoding.toByteArray())
            }

            body.contentType()?.let { contentType ->
                outputStream.write("Content-Type:${contentType.value}".toByteArray())
                outputStream.write(CRLF)
            }

            val contentLength = body.contentLength()
            contentLength.takeIf { it != 1L }?.let { length ->
                outputStream.write("Content-Length: $length".toByteArray())
                outputStream.write(CRLF)
            }

            outputStream.write(CRLF)
            body.writeTo(outputStream)
            outputStream.write(CRLF)
        }

        outputStream.write(DASH)
        outputStream.write(boundary.toByteArray())
        outputStream.write(DASH)
        outputStream.write(CRLF)
    }

    class Builder(private val boundary: String = UUID.randomUUID().toString()) {

        private val parts = mutableListOf<Part>()


        fun addPart(part: Part) {
            parts.add(part)
        }

        fun addPart(name: String, encoding: String?, file: File) {
            addPart(Part.create(name, encoding, file))
        }

        fun addPart(
            name: String,
            encoding: String? = null,
            requestBody: RequestBody
        ) {
            addPart(Part.create(name, null, encoding, requestBody))
        }

        fun addPart(
            name: String,
            encoding: String? = null,
            content: String
        ) {
            addPart(name, encoding, create(content, null))
        }

        fun build() = MultipartBody(boundary, parts)
    }

    class Part private constructor(
        val name: String,
        val fileName: String?,
        val encoding: String?,
        val requestBody: RequestBody
    ) {
        companion object {

            fun create(name: String, encoding: String?, file: File): Part {
                return create(
                    name,
                    file.name,
                    encoding,
                    create(ContentType.MULTIPART_FORM_DATA, file)
                )
            }

            fun create(
                name: String,
                fileName: String? = null,
                encoding: String? = null,
                requestBody: RequestBody
            ) = Part(name, fileName, encoding, requestBody)

        }
    }
}