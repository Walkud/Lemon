package com.lemon.app.bean

/**
 * Describe:
 * Created by liya.zhu on 2022/3/25
 */
data class TstResult(val code: Int, val text: String?, val tst: String?) {

    fun isSuccess() = code == 200
}