package com.tube.http.interceptor

import com.tube.http.request.Request
import com.tube.http.request.Response

/**
 * Describe:应用链，开始执行链式调用
 * Created by liya.zhu on 2022/3/18
 */
internal class ApplyChain(
    private val interceptor: List<Interceptor>,
    private val nextIndex: Int,
    private val originalRequest: Request
) : Interceptor.Chain {

    companion object {

        /**
         * 开始执行
         */
        fun proceed(interceptor: List<Interceptor>, originalRequest: Request): Response {
            return ApplyChain(interceptor, 0, originalRequest).proceed(originalRequest)
        }
    }

    override fun request() = originalRequest

    /**
     * 实现链式调用
     */
    override fun proceed(request: Request): Response {
        val applyChain = ApplyChain(interceptor, nextIndex + 1, request)
        return interceptor[nextIndex].intercept(applyChain)
    }
}