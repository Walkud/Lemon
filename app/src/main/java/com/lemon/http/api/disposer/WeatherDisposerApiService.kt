package com.lemon.http.api.disposer

import com.lemon.http.Api
import com.lemon.http.ApiField
import com.lemon.http.ApiPath
import com.lemon.http.ApiUrl
import com.lemon.http.annotations.WeatherApi
import com.lemon.http.bean.WeatherResult
import com.lemon.http.disposer.Disposer
import com.lemon.http.request.HttpMethod

/**
 * 中国气象天气预报 Api
 */
@WeatherApi
@ApiUrl("http://www.weather.com.cn/")
interface WeatherDisposerApiService {

    /**
     * 获取城市天气预报
     * @param cityCode 城市编码
     * @param createTime 请求时间戳(可以不要，这里只是示范请求添加参数方式)
     */
    @Api("data/cityinfo/{cityCode}.html", method = HttpMethod.GET)
    fun getCityWeatherInfo(
        @ApiPath("cityCode") cityCode: String,
        @ApiField("createTime") createTime: Long
    ): Disposer<WeatherResult>
}