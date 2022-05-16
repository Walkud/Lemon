package com.lemon.http.net

import com.lemon.http.BuildConfig
import com.lemon.http.Lemon
import com.lemon.http.api.TstApiService
import com.lemon.http.api.WeatherApiService
import com.lemon.http.api.disposer.TstDisposerApiService
import com.lemon.http.api.disposer.WeatherDisposerApiService
import com.lemon.http.client.LemonClient
import com.lemon.http.disposer.adapter.DisposerApiAdapterFactory
import com.lemon.http.factory.GsonConverterFactory
import com.lemon.http.interceptor.Interceptor
import com.lemon.http.log.LemonLogInterceptor
import com.lemon.http.log.LemonLogLevel
import com.lemon.http.request.Response
import java.util.*

/**
 * Lemon 实例构建类
 */
object Net {
    //https://api.btstu.cn 搏天api-高速稳定免费APi接口调用平台
    private val lemon = Lemon.build {
        //设置 ApiBaseUrl
        setApiUrl("https://api.btstu.cn")
        //添加 Gson 转换工厂
        addConverterFactory(GsonConverterFactory())
        //添加 Disposer 返回类型转换工厂
        addApiAdapterFactory(DisposerApiAdapterFactory())
        //添加请求拦截器
        addInterceptor(object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val request = chain.request()
                val newRequest =
                    request.newBuilder()
                        .addHeader("X-CALL-ID", UUID.randomUUID().toString())
                        .build()
                return chain.proceed(newRequest)
            }
        })
        if (BuildConfig.DEBUG) {
            //添加日志打印拦截器
            addInterceptor(LemonLogInterceptor(LemonLogLevel.BODY))
        }
        //设置 HttpClient
        setHttpClient(
            LemonClient.Builder().setReadTimeout(30 * 1000).setConnectTimeout(30 * 1000).build()
        )
    }

    fun getTstApiService() = lemon.create(TstApiService::class.java)

    fun getWeatherApiService() = lemon.create(WeatherApiService::class.java)

    fun getTstDisposerApiService() = lemon.create(TstDisposerApiService::class.java)

    fun getWeatherDisposerApiService() = lemon.create(WeatherDisposerApiService::class.java)
}