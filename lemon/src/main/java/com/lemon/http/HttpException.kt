package com.lemon.http

import com.lemon.http.request.Response

/**
 * Describe: Http 异常统一封装类
 * Created by liya.zhu on 2022/3/15
 */
class HttpException : RuntimeException {
    
    constructor(message: String) : super(message)

    constructor(message: String, cause: Throwable) : super(message, cause)

    companion object {
        fun create(msg: String, e: Throwable) = HttpException("$msg: ${e.message}", e)

        fun create(response: Response) =
            HttpException("Http ${response.code} error! url:${response.request.url}")
    }
}