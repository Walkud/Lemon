package com.tube.http.request.body

import com.tube.http.request.ContentType

/**
 * Describe: 请求响应消息体
 * Created by liya.zhu on 2022/3/2
 */
abstract class ResponseBody {

    companion object {

        val EMPTY_BODY = create(ByteArray(0))

        fun create(content: ByteArray, contentType: ContentType? = null): ResponseBody {
            return object : ResponseBody() {
                override fun contentType() = contentType

                override fun byteArray() = content

                override fun contentLength(): Long {
                    return content.size.toLong()
                }
            }
        }
    }

    abstract fun contentType(): ContentType?

    abstract fun byteArray(): ByteArray

    open fun contentLength() = -1L
}