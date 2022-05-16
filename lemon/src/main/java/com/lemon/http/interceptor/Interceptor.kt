package com.lemon.http.interceptor

import com.lemon.http.request.Request
import com.lemon.http.request.Response
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
}