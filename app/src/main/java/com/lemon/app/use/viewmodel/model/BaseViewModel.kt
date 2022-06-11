package com.lemon.app.use.viewmodel.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class BaseViewModel : ViewModel() {

    var showToast = MutableLiveData<String>()
    var showProgress = MutableLiveData<Boolean>()

    fun request(
        uiChangeLiveData: MutableLiveData<Boolean>? = null,
        callBlock: () -> Unit,
        errorBlock: ((e: Exception) -> Unit)? = null
    ) {
        viewModelScope.launch {
            uiChangeLiveData?.postValue(true)
            try {
                withContext(Dispatchers.IO) {
                    callBlock()
                }
            } catch (e: Exception) {
                errorBlock?.invoke(e)
            }
            uiChangeLiveData?.postValue(false)
        }
    }

}