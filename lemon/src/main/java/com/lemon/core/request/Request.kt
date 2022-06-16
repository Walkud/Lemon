package com.lemon.core.request

import com.lemon.core.isHttpProtocol
import com.lemon.core.request.body.RequestBody
import java.lang.reflect.Method

/**
 * Describe: 请求实体
 * Created by liya.zhu on 2022/3/3
 */
class Request private constructor(
    val originService: Class<*>,
    val originMethod: Method,
    val url: String,
    val httpMethod: HttpMethod,
    val headers: Headers,
    val body: RequestBody?
) {

    /**
     * 是否有 Body
     */
    fun hasBody() = httpMethod.hasBody()

    fun newBuilder(): Builder {
        return Builder(
            originService,
            originMethod,
            url,
            httpMethod,
            headers.newBuilder(),
            body
        )
    }

    class Builder(
        private val originService: Class<*>,
        private val originMethod: Method,
        private var url: String,
        private var httpMethod: HttpMethod,
        private val headersBuilder: Headers.Builder,
        private var body: RequestBody? = null
    ) {

        /**
         * 设置 Request Url
         */
        fun url(url: String) = apply {
            if (!url.isHttpProtocol()) {
                throw IllegalArgumentException("url must be HTTP or HTTPS!")
            }
            this.url = url
        }

        /**
         * 设置请求头参数
         */
        fun setHeader(key: String, value: String) = apply { headersBuilder.set(key, value) }

        /**
         * 添加请求头参数
         */
        fun addHeader(key: String, value: String) = apply { headersBuilder.add(key, value) }

        /**
         * 移除请求头参数
         */
        fun removeHeader(key: String) = apply { headersBuilder.remove(key) }

        /**
         * 设置请求方式
         */
        fun method(httpMethod: HttpMethod, body: RequestBody? = null) = apply {
            this.httpMethod = httpMethod

            if (body == null && httpMethod.hasBody()) {
                throw IllegalArgumentException("Method: $httpMethod must have a RuquestBody!")
            }

            body?.let {
                setBody(it)
            }
        }

        /**
         * 设置请求消息体
         */
        fun setBody(body: RequestBody?) = apply { this.body = body }

        fun build(): Request {
            return Request(
                originService,
                originMethod,
                url,
                httpMethod,
                headersBuilder.build(),
                body
            )
        }
    }
}