package com.tube.http.request.body

import com.tube.http.request.ContentType
import java.io.OutputStream

/**
 * Describe: 请求消息体，参考 OkHttp RequestBody
 *
 * Created by liya.zhu on 2022/3/7
 */
abstract class RequestBody {

    companion object {

        val EMPTY_BODY = create(ByteArray(0))

        fun create(content: String, contentType: ContentType? = null): RequestBody {
            val charset = contentType?.let {
                it.getCharset()
            } ?: Charsets.UTF_8

            return create(content.toByteArray(charset), contentType)
        }

        fun create(content: ByteArray, contentType: ContentType? = null) =
            create(content, contentType, 0)

        fun create(content: ByteArray, contentType: ContentType?, offset: Int): RequestBody {
            return object : RequestBody() {
                override fun contentType() = contentType

                override fun writeTo(outputStream: OutputStream) {
                    outputStream.write(content, offset, content.size)
                }

                override fun contentLength(): Long {
                    return content.size.toLong()
                }
            }
        }
    }

    abstract fun contentType(): ContentType?

    abstract fun writeTo(outputStream: OutputStream)

    open fun contentLength() = -1L

}