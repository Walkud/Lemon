package com.lemon.core.request

import com.lemon.core.request.body.ResponseBody

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

    /**
     * 获取头参数
     */
    fun getHeader(key: String) = headers.getFirst(key)

    /**
     * 构建新 Response Builder
     */
    fun newBuilder() = Builder(request, code, headers, body)

    /**
     * 是否存在 Body 数据
     */
    fun hasBodyData(): Boolean {
        return body.contentLength() > 0
                || "chunked".equals(getHeader("Transfer-Encoding"), ignoreCase = true)
    }

    class Builder(
        var request: Request,
        var code: Int,
        var headers: Headers,
        var body: ResponseBody? = null
    ) {

        fun setRequest(request: Request) = apply { this.request = request }

        fun setCode(code: Int) = apply { this.code = code }

        fun setHeaders(headers: Headers) = apply { this.headers = headers }

        fun setBody(body: ResponseBody) = apply { this.body = body }

        fun build(): Response {
            val finalCode =
                if (code >= 0) code else throw IllegalArgumentException("Response builder error: code < 0 (code:$code) !")
            val finalBody = body ?: ResponseBody.EMPTY_BODY
            return Response(request, finalCode, headers, finalBody)
        }

    }

}