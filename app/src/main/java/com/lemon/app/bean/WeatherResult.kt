package com.lemon.app.bean

//{"weatherinfo":{"city":"北京","cityid":"101010100","temp1":"18℃","temp2":"31℃","weather":"多云转阴","img1":"n1.gif","img2":"d2.gif","ptime":"18:00"}}
data class WeatherResult(val weatherinfo: WeatherInfo?)

data class WeatherInfo(
    val city: String?,
    val cityid: String?,
    val temp1: String?,
    val temp2: String?,
    val weather: String?,
    val img1: String?,
    val img2: String?,
    val ptime: String?
)

