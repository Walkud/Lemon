package com.tube.http.apimethod

import com.tube.http.HttpException
import com.tube.http.Tube
import com.tube.http.adapter.ApiAdapter
import com.tube.http.converter.Converter
import com.tube.http.interceptor.ApplyChain
import com.tube.http.referenceName
import com.tube.http.request.body.ResponseBody

/**
 * Describe: Api 请求处理类
 * Created by liya.zhu on 2022/3/2
 */
internal class ApiMethod(
    private val tube: Tube,
    private val apiMethodParser: ApiMethodParser,
    private val responseConverter: Converter<ResponseBody, *>,
    private val apiAdapter: ApiAdapter
) {

    /**
     * 处理 Api Service 方法实际调用
     */
    fun invoke(args: Array<Any?>) = apiAdapter.adapt { proceed(args) }

    /**
     * 执行处理
     * 1、构建 Request
     * 2、调用实际请求处理逻辑
     * 3、转换 response
     */
    private fun proceed(args: Array<Any?>): Any {
        val request = apiMethodParser.buildRequest(args)
        val response = ApplyChain.proceed(tube.interceptors, request)

        if (response.isSuccess()) {
            val converValue = responseConverter.convert(response.body)
            if (converValue != null) {
                return converValue
            }

            throw IllegalArgumentException(
                "The result of the response converter conversion is null" +
                        "for method:${apiMethodParser.originMethod.referenceName()}" +
                        ",response converter:${responseConverter::class.java.name}"
            )
        }

        throw HttpException.create(response)
    }
}