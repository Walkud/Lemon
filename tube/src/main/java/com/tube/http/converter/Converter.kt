package com.tube.http.converter

import com.tube.http.request.body.RequestBody
import com.tube.http.request.body.ResponseBody
import java.io.IOException
import java.lang.reflect.Method
import java.lang.reflect.Type

/**
 * Describe: 数据对象转换器，用于转换服务端与客户端约定的数据传输与接收形式
 * Created by liya.zhu on 2022/3/2
 */
interface Converter<T, R> {

    @Throws(IOException::class)
    fun convert(value: T): R

    /**
     * 转换器工厂
     */
    interface Factory {
        /**
         * 生成请求体转换器
         */
        fun requestBodyConverter(type: Type, method: Method): Converter<*, RequestBody>?

        /**
         * 生成请求响应消息体转换器
         */
        fun responseBodyConverter(type: Type, method: Method): Converter<ResponseBody, *>?
    }
}