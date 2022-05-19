package com.lemon.http.use.viewmodel.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.lemon.http.net.Net
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.Exception

/**
 * 城市天气预报 ViewModel
 */
class WeatherViewModel : BaseViewModel() {

    var cityWeatherResult = MutableLiveData<String>()

    fun getCityWeatherInfo(cityCode: String) {
        request(showProgress, {
            val createTime = System.currentTimeMillis()
            val result = Net.getWeatherApiService().getCityWeatherInfo(cityCode, createTime)
            cityWeatherResult.postValue(Gson().toJson(result))
        }, { showToast.postValue("getCityWeatherInfo Exception :${it.message}") })
    }
}