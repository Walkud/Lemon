package com.tube.http.interceptor

import com.tube.http.TubeUtils
import com.tube.http.client.HttpClient
import com.tube.http.request.Headers
import com.tube.http.request.Response
import com.tube.http.toURL

/**
 * Describe: 实际发起请求调用的拦截器
 * 请求头参数添加参考 OkHttp
 * @see <a href="https://github.com/square/okhttp">OkHttp</a>
 * Created by liya.zhu on 2022/3/18
 */
class RealCallInterceptor(private val httpClient: HttpClient) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val headers = request.headers
        val newBuilder = request.newBuilder()

        val userAgent = headers.get(Headers.USER_AGENT_KEY)
        if (userAgent == null) {
            newBuilder.setHeader(Headers.USER_AGENT_KEY, TubeUtils.userAgent)
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

        return httpClient.execute(newBuilder.build())
    }
}