package com.lemon.core.request.body

import com.lemon.core.request.ContentType
import java.util.zip.GZIPInputStream

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

        /**
         * 创建 Gzip 解压 ResponseBody
         */
        fun createGzip(content: ByteArray, contentType: ContentType? = null): ResponseBody {
            val gzipInputStream = GZIPInputStream(content.inputStream())
            val unGzipContent = gzipInputStream.readBytes()
            return create(unGzipContent, contentType)
        }
    }

    abstract fun contentType(): ContentType?

    abstract fun byteArray(): ByteArray

    open fun contentLength() = -1L
}