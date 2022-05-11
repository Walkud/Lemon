package com.tube.http.use.viewmodel.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.tube.http.net.Net
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.Exception

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