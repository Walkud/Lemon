package com.tube.http.api

import com.tube.http.Api
import com.tube.http.ApiField
import com.tube.http.bean.TstResult

/**
 * 搏天api-语言翻译
 */
interface TstApiService {

    @Api("tst/api.php")
    fun languageTranslation(@ApiField("text") text: String): TstResult
}