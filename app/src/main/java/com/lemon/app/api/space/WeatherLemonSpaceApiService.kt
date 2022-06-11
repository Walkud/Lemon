package com.lemon.app.api.space

import com.lemon.space.LemonSpace
import com.lemon.core.Api
import com.lemon.core.ApiField
import com.lemon.core.ApiPath
import com.lemon.core.ApiUrl
import com.lemon.app.annotations.WeatherApi
import com.lemon.app.bean.WeatherResult
import com.lemon.core.request.HttpMethod

/**
 * 中国气象天气预报 Api
 */
@WeatherApi
@ApiUrl("http://www.weather.com.cn/")
interface WeatherLemonSpaceApiService {

    /**
     * 获取城市天气预报
     * @param cityCode 城市编码
     * @param createTime 请求时间戳(可以不要，这里只是示范请求添加参数方式)
     */
    @Api("data/cityinfo/{cityCode}.html", method = HttpMethod.GET)
    fun getCityWeatherInfo(
        @ApiPath("cityCode") cityCode: String,
        @ApiField("createTime") createTime: Long
    ): LemonSpace<WeatherResult>
}