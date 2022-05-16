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
 * 语言翻译 ViewModel
 */
class TstViewModel : ViewModel() {

    var translationResult = MutableLiveData<String>()

    fun languageTranslation(text: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val translationText = try {
                val result = Net.getTstApiService().languageTranslation(text)
                Gson().toJson(result)
            } catch (e: Exception) {
                "languageTranslation Exception :${e.message}"
            }
            translationResult.postValue(translationText)
        }
    }
}