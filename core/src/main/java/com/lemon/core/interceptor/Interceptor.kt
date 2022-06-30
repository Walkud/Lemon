package com.lemon.core.interceptor

import com.lemon.core.request.Headers
import com.lemon.core.request.Request
import com.lemon.core.request.Response
import com.lemon.core.request.body.ResponseBody
import java.io.IOException
import kotlin.jvm.Throws

/**
 * Describe: 请求拦截器，用于统一处理请求头增删改，请求参数加解密等场景
 * Created by liya.zhu on 2022/3/3
 */
interface Interceptor {

    @Throws(IOException::class)
    fun intercept(chain: Chain): Response

    interface Chain {
        fun request(): Request

        @Throws(IOException::class)
        fun proceed(request: Request): Response
    }

    companion object {

        fun createGzipInterceptor(): Interceptor {
            return object : Interceptor {
                override fun intercept(chain: Chain): Response {
                    val request = chain.request()
                    val newRequestBuilder = request.newBuilder()

                    var allowGzip = false
                    if (request.getHeader(Headers.ACCEPT_ENCODING) == null && request.getHeader(
                            Headers.RANGE
                        ) == null
                    ) {
                        //请求头中未设置支持的编码方式，在请求头中添加 Gzip 的编码方式
                        allowGzip = true
                        newRequestBuilder.addHeader(Headers.ACCEPT_ENCODING, "gzip")
                    }

                    val response = chain.proceed(newRequestBuilder.build())

                    if (allowGzip) {
                        val headerValue = response.getHeader(Headers.CONTENT_ENCODING)
                        if ("gzip".equals(headerValue, true) && response.hasBodyData()) {
                            val newResponse = response.newBuilder()
                            newResponse.body = ResponseBody.createGzip(
                                response.body.byteArray(),
                                response.headers.getContentType()
                            )
                            return newResponse.build()
                        }
                    }

                    return response
                }
            }
        }
    }
}