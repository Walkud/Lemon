package com.tube.http.apimethod

import com.tube.http.HttpException
import com.tube.http.TubeHttp
import com.tube.http.disposer.Disposer
import com.tube.http.disposer.transformer.ConvertTransformer
import com.tube.http.interceptor.ApplyChain
import com.tube.http.referenceName
import com.tube.http.request.Request

/**
 * Describe: Api 请求处理类
 * Created by liya.zhu on 2022/3/2
 */
class ApiMethod(private val tubeHttp: TubeHttp, private val apiMethodParser: ApiMethodParser) {

    /**
     * 处理 Api Service 方法实际调用
     */
    fun invoke(args: Array<Any?>): Any {
        return Disposer.create(args)
            .convert(object : ConvertTransformer<Array<Any?>, Request> {
                override fun convert(result: Array<Any?>): Disposer<Request> {
                    val request = apiMethodParser.buildRequest(args)
                    return Disposer.create(request)
                }
            })
            .convert(object : ConvertTransformer<Request, Any> {
                override fun convert(result: Request): Disposer<Any> {
                    val response = ApplyChain.proceed(tubeHttp.interceptors, result)

                    if (response.isSuccess()) {
                        val responseConverter = apiMethodParser.responseConverter

                        val converValue = responseConverter.convert(response.body)
                        if (converValue != null) {
                            return Disposer.create(converValue)
                        }

                        throw IllegalArgumentException(
                            "The result of the response converter conversion is null" +
                                    "for method:${apiMethodParser.originMethod.referenceName()}" +
                                    ",response converter:${responseConverter::class.java.name}"
                        )
                    }

                    throw HttpException.create(response)
                }
            })
    }
}