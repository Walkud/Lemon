package com.lemon.core.client

import com.lemon.core.LemonUtils
import com.lemon.core.request.Headers
import com.lemon.core.request.Request
import com.lemon.core.request.Response
import com.lemon.core.toURL
import java.io.IOException
import java.net.Proxy
import kotlin.jvm.Throws

/**
 * Describe: Http 请求客户端接口
 * Http 请求客户端可自定义，也可以使用默认 LemonClient
 * Created by liya.zhu on 2022/3/2
 */
interface HttpClient {

    @Throws(IOException::class)
    fun execute(request: Request): Response

    /**
     * 读取超时时间
     */
    fun readTimeout(): Int

    /**
     * 连接超时时间
     */
    fun connectTimeout(): Int

    /**
     * 网络代理
     */
    fun proxy(): Proxy?

    companion object {

        /**
         * 添加默认请求头
         * 请求头参数添加参考 OkHttp
         * @see <a href="https://github.com/square/okhttp">OkHttp</a>
         * Created by liya.zhu on 2022/4/26
         */
        fun addDefaultHeaders(request: Request): Request {
            val headers = request.headers
            val newBuilder = request.newBuilder()

            val userAgent = headers.get(Headers.USER_AGENT_KEY)
            if (userAgent == null) {
                newBuilder.setHeader(Headers.USER_AGENT_KEY, LemonUtils.userAgent)
            }

            val host = headers.get(Headers.HOST_KEY)
            if (host == null) {
                val url = request.url.toURL()
                newBuilder.setHeader(Headers.HOST_KEY, "${url.host}:${url.port}")
            }

            val connection = headers.get(Headers.CONNECTION_KEY)
            if (connection == null) {
                newBuilder.setHeader(Headers.CONNECTION_KEY, "Keep-Alive")
            }

            request.body?.let { body ->
                val contentType = body.contentType()
                contentType?.let {
                    newBuilder.setHeader(Headers.CONTENT_TYPE_KEY, it.value)
                }

                val contentLength = body.contentLength()
                if (contentLength != -1L) {
                    newBuilder.setHeader(Headers.CONTENT_LENGTH_KEY, "$contentLength")
                    newBuilder.removeHeader(Headers.TRANSFER_ENCODING_KEY)
                } else {
                    newBuilder.setHeader(Headers.TRANSFER_ENCODING_KEY, "chunked")
                    newBuilder.removeHeader(Headers.CONTENT_LENGTH_KEY)
                }
            }

            return newBuilder.build()
        }
    }

}