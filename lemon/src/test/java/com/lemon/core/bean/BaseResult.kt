package com.lemon.core.bean

/**
 * Describe:
 * Created by liya.zhu on 2022/3/25
 */
data class BaseResult<T>(val code: Int, val msg: String, val data: T) {

    fun isSuccess() = code == 0
}