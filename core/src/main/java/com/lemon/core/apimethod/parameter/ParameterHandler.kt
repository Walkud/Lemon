package com.lemon.core.apimethod.parameter

import com.lemon.core.LemonUtils
import com.lemon.core.converter.Converter
import com.lemon.core.request.RequestFactory
import com.lemon.core.request.body.RequestBody
import java.io.IOException
import java.lang.reflect.Method

/**
 * Describe: 参数处理器
 * 作用：方法参数解析时生成对应参数处理方式
 * Created by liya.zhu on 2022/3/7
 */
internal interface ParameterHandler<in T> {

    fun apply(requestFactory: RequestFactory, value: T?)

    /**
     * 处理 @ApiBody 注解实体类
     */
    class Body<T>(
        private val index: Int,
        private val method: Method,
        private val converter: Converter<T, RequestBody>
    ) :
        ParameterHandler<T> {

        override fun apply(requestFactory: RequestFactory, value: T?) {
            if (value != null) {
                try {
                    requestFactory.setBody(converter.convert(value))
                } catch (e: IOException) {
                    LemonUtils.parameterError(
                        index,
                        method,
                        "RequestBody convert error! value:$value", e
                    )
                }
            } else {
                LemonUtils.parameterError(
                    index,
                    method,
                    "ApiBody parameter value must not be null!"
                )
            }
        }
    }

    /**
     * 处理 @ApiField 注解，将参数添加到表单参数中
     */
    class Field(
        private val index: Int,
        private val method: Method,
        private val name: String,
        private val encoded: Boolean
    ) : ParameterHandler<Any> {

        override fun apply(requestFactory: RequestFactory, value: Any?) {
            value?.let {
                requestFactory.addFormField(name, it.toString(), encoded)
            }
        }
    }

    /**
     * 处理 @ApiHeader 注解，将对应 Map 中的参数全部添加到表单参数中
     */
    class FieldMap(
        private val index: Int,
        private val method: Method,
        private val encoded: Boolean
    ) : ParameterHandler<Map<String, Any?>> {

        override fun apply(requestFactory: RequestFactory, value: Map<String, Any?>?) {
            if (value != null) {
                for (entry in value.entries) {
                    val name = entry.key
                    val feildValue = entry.value
                    if (feildValue != null) {
                        requestFactory.addFormField(name, feildValue.toString(), encoded)
                    } else {
                        LemonUtils.parameterError(
                            index,
                            method,
                            "ApiField Map value is null for key:${entry.key}!"
                        )
                    }
                }
            } else {
                LemonUtils.parameterError(index, method, "ApiField Map value must not be null!")
            }
        }
    }

    /**
     * 处理 @ApiHeader 注解，将参数添加到请求头参数中
     */
    class Header(
        private val index: Int,
        private val method: Method,
        private val name: String,
    ) : ParameterHandler<Any> {
        override fun apply(requestFactory: RequestFactory, value: Any?) {
            value?.let {
                requestFactory.addHeader(name, it.toString())
            }
        }
    }

    /**
     * 处理 @ApiHeader 注解，将对应的 Map 中的参数全部添加到请求头参数中
     */
    class HeaderMap(
        private val index: Int,
        private val method: Method,
    ) : ParameterHandler<Map<String, Any?>> {
        override fun apply(requestFactory: RequestFactory, value: Map<String, Any?>?) {
            if (value != null) {
                for (entry in value.entries) {
                    val name = entry.key
                    val feildValue = entry.value
                    if (feildValue != null) {
                        requestFactory.addHeader(name, feildValue.toString())
                    } else {
                        LemonUtils.parameterError(
                            index,
                            method,
                            "ApiHeader Map value is null for key:${entry.key}!"
                        )
                    }
                }
            } else {
                LemonUtils.parameterError(index, method, "ApiHeader Map value must not be null!")
            }
        }
    }

    /**
     * 处理 @ApiPath 注解，将使用实参替换请求路径替换符
     */
    class Path(
        private val index: Int,
        private val method: Method,
        private val name: String,
        private val encoded: Boolean
    ) : ParameterHandler<Any> {
        override fun apply(requestFactory: RequestFactory, value: Any?) {
            val path = value ?: LemonUtils.parameterError(
                index,
                method,
                "Path $name value must not be null!"
            )
            requestFactory.addPathParam(name, path.toString(), encoded)
        }
    }

    /**
     * 处理 @Part 注解，将实参部件数据添加到请求中
     */
    class Part(
        private val index: Int,
        private val method: Method,
        private val name: String,
        private val encoding: String
    ) : ParameterHandler<Any> {
        override fun apply(requestFactory: RequestFactory, value: Any?) {
            value?.let { part ->
                requestFactory.addPart(name, encoding, part)
            }
        }
    }

    /**
     * 处理 @Part 注解，将实参部件 Map 数据添加到请求中
     */
    class PartMap(
        private val index: Int,
        private val method: Method,
        private val encoding: String
    ) : ParameterHandler<Map<String, Any?>> {
        override fun apply(requestFactory: RequestFactory, value: Map<String, Any?>?) {
            if (value != null) {
                for (entry in value.entries) {
                    val name = entry.key
                    val partValue = entry.value
                    partValue?.let { part ->
                        requestFactory.addPart(name, encoding, part)
                    }
                }
            } else {
                LemonUtils.parameterError(index, method, "ApiPart Map value must not be null!")
            }
        }
    }
}