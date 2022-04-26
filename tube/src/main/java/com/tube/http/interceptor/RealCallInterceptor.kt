package com.tube.http.interceptor

import com.tube.http.client.HttpClient
import com.tube.http.request.Response

/**
 * Describe: 实际发起请求调用的拦截器
 */
internal class RealCallInterceptor(private val httpClient: HttpClient) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return httpClient.execute(chain.request())
    }
}