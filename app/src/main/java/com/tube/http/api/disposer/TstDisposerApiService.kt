package com.tube.http.api.disposer

import com.tube.http.Api
import com.tube.http.ApiField
import com.tube.http.bean.TstResult
import com.tube.http.disposer.Disposer

/**
 * 搏天api-语言翻译
 */
interface TstDisposerApiService {

    @Api("tst/api.php")
    fun languageTranslation(@ApiField("text") text: String): Disposer<TstResult>
}