package com.tube.http.api.disposer

import com.tube.http.Api
import com.tube.http.ApiField
import com.tube.http.ApiPath
import com.tube.http.ApiUrl
import com.tube.http.bean.WeatherResult
import com.tube.http.disposer.Disposer
import com.tube.http.request.HttpMethod

/**
 * 中国气象天气预报 Api
 */
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