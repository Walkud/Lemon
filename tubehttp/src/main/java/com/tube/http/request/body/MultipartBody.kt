package com.tube.http.request.body

import com.tube.http.TubeUtils
import com.tube.http.request.ContentType
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream

/**
 * Describe:多部件请求 Body
 * Created by liya.zhu on 2022/4/6
 */
class MultipartBody private constructor(
    val boundary: String,
    val parts: List<Part>
) : RequestBody() {

    private val contentType by lazy {
        ContentType.MULTIPART_FORM_DATA.addParameter("boundary", boundary)
    }

    companion object {
        private val CRLF = "\r\n".toByteArray()
        private val DASH = "--".toByteArray()
    }

    override fun measureContentLength(): Long {
        return ByteArrayOutputStream().let {
            writeTo(it)
            it.size().toLong()
        }
    }

    override fun contentType() = contentType

    override fun writeTo(outputStream: OutputStream) {
        for (part in parts) {
            val body = part.requestBody

            outputStream.write(DASH)
            outputStream.write(boundary.toByteArray())
            outputStream.write(CRLF)

            val fileNameSb = StringBuilder()
            part.fileName?.let { fileName ->
                fileNameSb.append("; filename=\"")
                fileNameSb.append(fileName)
                fileNameSb.append("\"")
            }

            val disposition = "Content-Disposition:form-data; name=\"${part.name}\"$fileNameSb"
            outputStream.write(disposition.toByteArray())
            outputStream.write(CRLF)

            body.contentType()?.let { contentType ->
                outputStream.write("Content-Type:${contentType.value}".toByteArray())
                outputStream.write(CRLF)
            }

            part.encoding?.let { encoding ->
                val transferEncoding = "Content-Transfer-Encoding:$encoding"
                outputStream.write(transferEncoding.toByteArray())
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

    fun newBuilder() = Builder(boundary, parts.toMutableList())

    class Builder(
        private val boundary: String = TubeUtils.getRandomUUID32(),
        private val parts: MutableList<Part> = mutableListOf()
    ) {

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