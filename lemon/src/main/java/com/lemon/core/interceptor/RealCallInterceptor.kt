package com.lemon.core.interceptor

import com.lemon.core.client.HttpClient
import com.lemon.core.request.Response

/**
 * Describe: 实际发起请求调用的拦截器
 */
internal class RealCallInterceptor(private val httpClient: HttpClient) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return httpClient.execute(chain.request())
    }
}