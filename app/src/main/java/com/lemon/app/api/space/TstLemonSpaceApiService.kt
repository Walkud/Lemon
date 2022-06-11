package com.lemon.app.api.space

import com.lemon.space.LemonSpace
import com.lemon.core.Api
import com.lemon.core.ApiField
import com.lemon.app.bean.TstResult

/**
 * 搏天api-语言翻译
 */
interface TstLemonSpaceApiService {

    @Api("tst/api.php")
    fun languageTranslation(@ApiField("text") text: String): LemonSpace<TstResult>
}