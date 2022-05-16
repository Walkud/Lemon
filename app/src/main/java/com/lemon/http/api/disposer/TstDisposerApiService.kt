package com.lemon.http.api.disposer

import com.lemon.http.Api
import com.lemon.http.ApiField
import com.lemon.http.bean.TstResult
import com.lemon.http.disposer.Disposer

/**
 * 搏天api-语言翻译
 */
interface TstDisposerApiService {

    @Api("tst/api.php")
    fun languageTranslation(@ApiField("text") text: String): Disposer<TstResult>
}