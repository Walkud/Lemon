package com.lemon.app.net

import com.lemon.space.adapter.LemonSpaceApiAdapterFactory
import com.lemon.app.BuildConfig
import com.lemon.core.Lemon
import com.lemon.app.annotations.WeatherApi
import com.lemon.app.api.TstApiService
import com.lemon.app.api.WeatherApiService
import com.lemon.app.api.space.TstLemonSpaceApiService
import com.lemon.app.api.space.WeatherLemonSpaceApiService
import com.lemon.core.client.LemonClient
import com.lemon.app.factory.GsonConverterFactory
import com.lemon.core.interceptor.Interceptor
import com.lemon.log.LemonLogInterceptor
import com.lemon.log.LemonLogLevel
import com.lemon.core.request.Response
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
        //添加 LemonSpace 返回类型转换工厂
        addApiAdapterFactory(LemonSpaceApiAdapterFactory())
        // 搏天api 添加请求拦截器
        addInterceptor(object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                Thread.sleep(1000)//模拟耗时1秒
                val request = chain.request()
                val newRequest =
                    request.newBuilder()
                        .addHeader("X-CALL-ID", UUID.randomUUID().toString())
                        //对所有的url都追加个客户端时间的参数
                        .appendQueryParams("clientTime", "${System.currentTimeMillis()}")
                        .build()
                return chain.proceed(newRequest)
            }
        })
        addInterceptor(object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val request = chain.request()
                //根据自定义Api判断额外添加请求头等参数，也可以根据 url 判断
                val annotation = request.originService.getAnnotation(WeatherApi::class.java)
                val newRequest = annotation?.let {
                    request.newBuilder()
                        .addHeader("X-TOKEN", UUID.randomUUID().toString())
                        .build()
                } ?: request
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

    fun getTstLemonSpaceApiService() = lemon.create(TstLemonSpaceApiService::class.java)

    fun getWeatherLemonSpaceApiService() = lemon.create(WeatherLemonSpaceApiService::class.java)
}