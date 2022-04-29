package com.tube.http.converter

import com.tube.http.referenceName
import com.tube.http.request.body.RequestBody
import com.tube.http.request.body.ResponseBody
import java.lang.reflect.Method
import java.lang.reflect.Type

/**
 * Describe:数据对象转换器查找器，用于根据 Type 匹配合适的数据对象转换器
 * Created by liya.zhu on 2022/3/25
 */
internal class ConverterFinder private constructor(private val converterFactors: List<Converter.Factory>) {

    companion object {
        /**
         * 创建转换器查找器
         * @param converterFactors 扩展的转换器工厂
         */
        fun create(converterFactors: MutableList<Converter.Factory>): ConverterFinder {
            converterFactors.add(0, BuildInConverterFactory())
            return ConverterFinder(converterFactors)
        }
    }

    /**
     * 查找请求体数据对象转换器
     */
    fun findRequestBodyConverter(type: Type, originMethod: Method): Converter<*, RequestBody> {
        for (converterFactor in converterFactors) {
            val converter = converterFactor.requestBodyConverter(
                type, originMethod
            )

            if (converter != null) {
                return converter
            }
        }

        throw  IllegalArgumentException(
            "Could not find RequestBody converter " +
                    "for method:${originMethod.referenceName()},typeName:$type"
        )
    }

    /**
     * 查找请求响应消息体数据对象转换器
     */
    fun findResponseBodyConverter(type: Type, originMethod: Method): Converter<ResponseBody, *> {
        for (converterFactor in converterFactors) {
            val converter = converterFactor.responseBodyConverter(type, originMethod)

            if (converter != null) {
                return converter
            }
        }

        throw  IllegalArgumentException(
            "Could not locate ResponseBody converter!" +
                    "for method:${originMethod.referenceName()},typeName:$type"
        )
    }

    /**
     * 内置的请求体及响应消息体转换器，内置：String、RequestBody、ResponseBody 转换器
     */
    private class BuildInConverterFactory : Converter.Factory {

        /**
         * 返回默认响应消息体转换器
         */
        override fun responseBodyConverter(
            type: Type,
            method: Method
        ): Converter<ResponseBody, *>? {
            if (type === String::class.java) {
                return object : Converter<ResponseBody, String> {
                    override fun convert(value: ResponseBody): String {
                        val charset = value.contentType()?.getCharset() ?: Charsets.UTF_8
                        return String(value.byteArray(), charset)
                    }
                }
            } else if (type === ResponseBody::class.java) {
                return object : Converter<ResponseBody, ResponseBody> {
                    override fun convert(value: ResponseBody): ResponseBody {
                        return value
                    }
                }
            }
            return null
        }

        /**
         * 返回默认请求消息体转换器
         */
        override fun requestBodyConverter(type: Type, method: Method): Converter<*, RequestBody>? {
            if (type === String::class.java) {
                return object : Converter<Any, RequestBody> {
                    override fun convert(value: Any): RequestBody {
                        return RequestBody.create(value.toString())
                    }
                }
            } else if (type === RequestBody::class.java) {
                return object : Converter<RequestBody, RequestBody> {
                    override fun convert(value: RequestBody): RequestBody {
                        return value
                    }
                }
            }
            return null
        }
    }
}