package com.tube.http

import com.tube.http.client.TubeClient
import com.tube.http.client.HttpClient
import com.tube.http.converter.Converter
import com.tube.http.interceptor.Interceptor
import com.tube.http.apimethod.ApiMethodFactory
import com.tube.http.converter.ConverterFinder
import com.tube.http.interceptor.RealCallInterceptor
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * Describe: TubeHttp 是一款针对于 Android 使用注解形式使接口适用于 Http 请求的网络请求库。
 * Created by liya.zhu on 2022/3/2
 */
class TubeHttp private constructor(
    val baseUrl: String,
    val converterFinder: ConverterFinder,
    val interceptors: List<Interceptor>
) {

    private val apiMthodFactory = ApiMethodFactory(this)

    companion object {
        inline fun build(block: Builder.() -> Unit) = Builder().apply(block).build()
    }

    /**
     * 创建 Api 接口的动态代理类
     */
    fun <T> create(service: Class<T>): T {
        TubeUtils.checkServiceClass(service)
        return Proxy.newProxyInstance(
            service.classLoader, arrayOf<Class<*>>(service),
            object : InvocationHandler {
                private val emptyArgs = arrayOfNulls<Any>(0)

                @Throws(Throwable::class)
                override fun invoke(proxy: Any, method: Method, args: Array<Any?>?): Any? {
                    if (method.declaringClass == Any::class.java) {
                        return method.invoke(this, args)
                    }

                    if (method.checkDefault()) {//检查是否为默认方法
                        throw RuntimeException("TubeHttp does not support interface default methods!")
                    }

                    val params = args ?: emptyArgs
                    val tubeApiMethod = apiMthodFactory.create(service, method)
                    return tubeApiMethod.invoke(params)
                }
            }) as T
    }

    class Builder {
        private var baseUrl: String? = null
        private val converterFactors: MutableList<Converter.Factory> = mutableListOf()
        private val interceptors: MutableList<Interceptor> = mutableListOf()
        private var httpClient: HttpClient? = null

        /**
         * 设置请求 base url，例如：https://api.test.com
         */
        fun setBaseUrl(baseUrl: String) = apply { this.baseUrl = baseUrl }

        /**
         * 添加数据转换器工厂，通常是 json 格式的转换工厂
         */
        fun addConverterFactory(converter: Converter.Factory) =
            apply { converterFactors.add(converter) }

        /**
         * 添加请求拦截器
         */
        fun addInterceptor(interceptor: Interceptor) =
            apply { interceptors.add(interceptor) }

        /**
         * 设置 Http 客户端，可为空，默认为 TubeClient(使用 HttpURLConnection)
         */
        fun setTubeHttpClient(httpClient: HttpClient) = apply { this.httpClient = httpClient }

        fun build(): TubeHttp {

            val finalBaseUrl =
                baseUrl ?: throw IllegalArgumentException("TubeHttp baseUrl required!")

            if (!finalBaseUrl.isHttpProtocol()) {
                throw IllegalArgumentException("TubeHttp baseUrl must be HTTP or HTTPS!")
            }

            //如果未设置 HttpClient，则创建一个默认的 TubeClient
            val httpClient = httpClient ?: TubeClient.Builder().build()
            //添加请求实际调用拦截器
            interceptors.add(RealCallInterceptor(httpClient))

            return TubeHttp(
                finalBaseUrl,
                ConverterFinder.create(converterFactors),
                interceptors.toList()
            )
        }

    }
}