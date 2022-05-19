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
class TstViewModel : BaseViewModel() {

    var translationResult = MutableLiveData<String>()

    fun languageTranslation(text: String) {
        request(
            showProgress,
            callBlock = {
                val result = Net.getTstApiService().languageTranslation(text)
                translationResult.postValue(Gson().toJson(result))
            },
            errorBlock = { showToast.postValue("languageTranslation Exception :${it.message}") })
    }
}