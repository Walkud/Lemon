package com.lemon.http.api.space

import com.lemon.http.space.LemonSpace
import com.lemon.http.Api
import com.lemon.http.ApiField
import com.lemon.http.bean.TstResult

/**
 * 搏天api-语言翻译
 */
interface TstLemonSpaceApiService {

    @Api("tst/api.php")
    fun languageTranslation(@ApiField("text") text: String): LemonSpace<TstResult>
}