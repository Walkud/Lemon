package com.lemon.http.use.viewmodel.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.lemon.http.net.Net
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.Exception

class WeatherViewModel : ViewModel() {

    var cityWeatherResult = MutableLiveData<String>()

    fun getCityWeatherInfo(cityCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val translationText = try {
                val createTime = System.currentTimeMillis()
                val result = Net.getWeatherApiService().getCityWeatherInfo(cityCode, createTime)
                Gson().toJson(result)
            } catch (e: Exception) {
                "getCityWeatherInfo Exception :${e.message}"
            }
            cityWeatherResult.postValue(translationText)
        }
    }
}