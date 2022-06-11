package com.lemon.app.use.viewmodel.model

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.lemon.app.net.Net

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