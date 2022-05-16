package com.lemon.http.request.body

import com.lemon.http.readTo
import com.lemon.http.request.ContentType
import com.lemon.http.tryClose
import java.io.*

/**
 * Describe: 请求消息体，参考 OkHttp RequestBody
 *
 * Created by liya.zhu on 2022/3/7
 */
abstract class RequestBody {

    companion object {

        val EMPTY_BODY = create(ByteArray(0))

        fun create(content: String, contentType: ContentType? = null): RequestBody {
            val charset = contentType?.getCharset() ?: Charsets.UTF_8

            return create(content.toByteArray(charset), contentType)
        }

        fun create(content: ByteArray, contentType: ContentType? = null) =
            create(content, contentType, 0)

        fun create(content: ByteArray, contentType: ContentType? = null, offset: Int): RequestBody {
            return object : RequestBody() {
                override fun contentType() = contentType

                override fun writeTo(outputStream: OutputStream) {
                    outputStream.write(content, offset, content.size)
                }

                override fun measureContentLength(): Long {
                    return content.size.toLong()
                }
            }
        }

        fun create(contentType: ContentType? = null, file: File): RequestBody {
            return object : RequestBody() {
                override fun contentType() = contentType

                override fun writeTo(outputStream: OutputStream) {
                    val inputStream = FileInputStream(file)
                    inputStream.readTo(outputStream)
                    inputStream.tryClose()
                }

                override fun measureContentLength() = file.length()

            }
        }
    }

    private var contentLength = -1L

    abstract fun contentType(): ContentType?

    abstract fun writeTo(outputStream: OutputStream)

    abstract fun measureContentLength(): Long

    fun contentLength(): Long {
        if (contentLength == -1L) {
            contentLength = measureContentLength()
        }
        return contentLength
    }

}