package com.lemon.http.api

import com.lemon.http.Api
import com.lemon.http.ApiField
import com.lemon.http.bean.TstResult

/**
 * 搏天api-语言翻译
 */
interface TstApiService {

    @Api("tst/api.php")
    fun languageTranslation(@ApiField("text") text: String): TstResult
}