package com.tube.http.request

import com.tube.http.request.body.ResponseBody

/**
 * Describe: 请求响应实体
 * Created by liya.zhu on 2022/3/15
 */
class Response private constructor(
    val request: Request,
    val code: Int,
    val headers: Headers,
    val body: ResponseBody
) {

    /**
     * 是否成功
     */
    fun isSuccess() = code in 200..299

    class Builder {
        private var request: Request? = null
        private var code = -1
        private var headers: Headers? = null
        private var body: ResponseBody? = null

        fun setRequest(request: Request) = apply { this.request = request }

        fun setCode(code: Int) = apply { this.code = code }

        fun setHeaders(headers: Headers) = apply { this.headers = headers }

        fun setBody(body: ResponseBody?) = apply { this.body = body }

        fun build(): Response {
            val finalRequest = request
                ?: throw IllegalArgumentException("Response builder error: request is null!")
            val finalCode =
                if (code >= 0) code else throw IllegalArgumentException("Response builder error: code < 0 (code:$code) !")
            val finalHeaders = headers ?: Headers.Builder().build()
            val finalBody = body ?: ResponseBody.EMPTY_BODY

            return Response(finalRequest, finalCode, finalHeaders, finalBody)
        }

    }

}