package com.lemon.http

import com.lemon.http.adapter.ApiAdapter
import com.lemon.http.adapter.ApiAdapterFinder
import com.lemon.http.client.LemonClient
import com.lemon.http.client.HttpClient
import com.lemon.http.converter.Converter
import com.lemon.http.interceptor.Interceptor
import com.lemon.http.apimethod.ApiMethodFactory
import com.lemon.http.converter.ConverterFinder
import com.lemon.http.interceptor.RealCallInterceptor
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * Describe: Lemon 是一款针对于 Android 使用注解形式使接口适用于 Http 请求的网络请求库。
 * Created by liya.zhu on 2022/3/2
 */
class Lemon private constructor(
    internal val apiUrl: String,
    internal val converterFinder: ConverterFinder,
    internal val apiAdapterFinder: ApiAdapterFinder,
    internal val interceptors: List<Interceptor>
) {

    private val apiMthodFactory = ApiMethodFactory(this)

    companion object {
        inline fun build(block: Builder.() -> Unit) = Builder().apply(block).build()
    }

    /**
     * 创建 Api 接口的动态代理类
     */
    fun <T> create(service: Class<T>): T {
        LemonUtils.checkServiceClass(service)
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
                        throw RuntimeException("Lemon does not support interface default methods!")
                    }

                    val params = args ?: emptyArgs
                    val apiMethod = apiMthodFactory.create(service, method)
                    return apiMethod.invoke(params)
                }
            }) as T
    }

    class Builder {
        private var apiUrl: String? = null
        private val apiAdapterFactorys: MutableList<ApiAdapter.Factory> = mutableListOf()
        private val converterFactors: MutableList<Converter.Factory> = mutableListOf()
        private val interceptors: MutableList<Interceptor> = mutableListOf()
        private var httpClient: HttpClient? = null

        /**
         * 设置请求 api url，例如：https://api.test.com
         */
        fun setApiUrl(apiUrl: String) = apply { this.apiUrl = apiUrl }

        /**
         * 添加 Api 适配器工厂，用于转换返回结果类型
         */
        fun addApiAdapterFactory(apiAdapterFactory: ApiAdapter.Factory) = apply {
            apiAdapterFactorys.add(apiAdapterFactory)
        }

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
         * 设置 Http 客户端，可为空，默认为 LemonClient(使用 HttpURLConnection)
         */
        fun setHttpClient(httpClient: HttpClient) = apply { this.httpClient = httpClient }

        fun build(): Lemon {

            val finalApiUrl =
                apiUrl ?: throw IllegalArgumentException("Lemon apiUrl required!")

            if (!finalApiUrl.isHttpProtocol()) {
                throw IllegalArgumentException("Lemon apiUrl must be HTTP or HTTPS!")
            }

            //如果未设置 HttpClient，则创建一个默认的 LemonClient
            val httpClient = httpClient ?: LemonClient.Builder().build()
            //添加请求实际调用拦截器
            interceptors.add(RealCallInterceptor(httpClient))

            return Lemon(
                finalApiUrl,
                ConverterFinder.create(converterFactors),
                ApiAdapterFinder.create(apiAdapterFactorys),
                interceptors.toList()
            )
        }
    }
}