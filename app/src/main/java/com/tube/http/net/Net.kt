package com.tube.http.net

import com.tube.http.BuildConfig
import com.tube.http.Tube
import com.tube.http.api.TstApiService
import com.tube.http.api.WeatherApiService
import com.tube.http.api.disposer.TstDisposerApiService
import com.tube.http.api.disposer.WeatherDisposerApiService
import com.tube.http.client.TubeClient
import com.tube.http.disposer.adapter.DisposerApiAdapterFactory
import com.tube.http.factory.GsonConverterFactory
import com.tube.http.interceptor.Interceptor
import com.tube.http.log.TubeLogInterceptor
import com.tube.http.log.TubeLogLevel
import com.tube.http.request.Response
import java.util.*

object Net {
    //https://api.btstu.cn 搏天api-高速稳定免费APi接口调用平台
    private val tube = Tube.build {
        setApiUrl("https://api.btstu.cn")
        addConverterFactory(GsonConverterFactory())
        addApiAdapterFactory(DisposerApiAdapterFactory())
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
            addInterceptor(TubeLogInterceptor(TubeLogLevel.BODY))
        }
        setTubeClient(
            TubeClient.Builder().setReadTimeout(30 * 1000).setConnectTimeout(30 * 1000).build()
        )
    }

    fun getTstApiService() = tube.create(TstApiService::class.java)

    fun getWeatherApiService() = tube.create(WeatherApiService::class.java)

    fun getTstDisposerApiService() = tube.create(TstDisposerApiService::class.java)

    fun getWeatherDisposerApiService() = tube.create(WeatherDisposerApiService::class.java)
}