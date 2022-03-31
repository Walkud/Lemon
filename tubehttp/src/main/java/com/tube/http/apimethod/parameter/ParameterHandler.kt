package com.tube.http.apimethod.parameter

import com.tube.http.TubeUtils
import com.tube.http.converter.Converter
import com.tube.http.request.Request
import com.tube.http.request.body.RequestBody
import java.io.IOException
import java.lang.reflect.Method

/**
 * Describe: 参数处理器
 * 作用：方法参数解析时生成对应参数处理方式
 * Created by liya.zhu on 2022/3/7
 */
interface ParameterHandler<in T> {

    fun apply(builder: Request.Builder, value: T?)

    /**
     * 处理 @Body 注解实体类
     */
    class Body<T>(
        private val index: Int,
        private val method: Method,
        private val converter: Converter<T, RequestBody>
    ) :
        ParameterHandler<T> {

        override fun apply(builder: Request.Builder, value: T?) {
            if (value != null) {
                try {
                    builder.setBody(converter.convert(value))
                } catch (e: IOException) {
                    TubeUtils.parameterError(
                        index,
                        method,
                        "RequestBody convert error! value:$value", e
                    )
                }
            } else {
                TubeUtils.parameterError(
                    index,
                    method,
                    "Body parameter value must not be null!"
                )
            }
        }
    }

    /**
     * 处理 @Field 注解，将参数添加到表单参数中
     */
    class Field(
        private val index: Int,
        private val method: Method,
        var name: String,
        private val encoded: Boolean
    ) : ParameterHandler<Any> {

        override fun apply(builder: Request.Builder, value: Any?) {
            value?.let {
                builder.addFormField(name, it.toString(), encoded)
            }
        }
    }

    /**
     * 处理 @FieldMap 注解，将对应 Map 中的参数全部添加到表单参数中
     */
    class FieldMap(
        private val index: Int,
        private val method: Method,
        private val encoded: Boolean
    ) : ParameterHandler<Map<String, Any?>> {

        override fun apply(builder: Request.Builder, value: Map<String, Any?>?) {
            if (value != null) {
                for (entry in value.entries) {
                    val name = entry.key
                    val feildValue = entry.value
                    if (feildValue != null) {
                        builder.addFormField(name, feildValue.toString(), encoded)
                    } else {
                        TubeUtils.parameterError(
                            index,
                            method,
                            "FieldMap value is null for key:${entry.key}!"
                        )
                    }
                }
            } else {
                TubeUtils.parameterError(index, method, "FieldMap value must not be null!")
            }
        }
    }

    /**
     * 处理 @Header 注解，将参数添加到请求头参数中
     */
    class Header(
        private val index: Int,
        private val method: Method,
        var name: String,
    ) : ParameterHandler<Any> {
        override fun apply(builder: Request.Builder, value: Any?) {
            value?.let {
                builder.addHeader(name, it.toString())
            }
        }
    }

    /**
     * 处理 @HeaderMap 注解，将对应的 Map 中的参数全部添加到请求头参数中
     */
    class HeaderMap(
        private val index: Int,
        private val method: Method,
    ) : ParameterHandler<Map<String, Any?>> {
        override fun apply(builder: Request.Builder, value: Map<String, Any?>?) {
            if (value != null) {
                for (entry in value.entries) {
                    val name = entry.key
                    val feildValue = entry.value
                    if (feildValue != null) {
                        builder.addHeader(name, feildValue.toString())
                    } else {
                        TubeUtils.parameterError(
                            index,
                            method,
                            "HeaderMap value is null for key:${entry.key}!"
                        )
                    }
                }
            } else {
                TubeUtils.parameterError(index, method, "HeaderMap value must not be null!")
            }
        }
    }

    /**
     * 处理 @Path 注解，将使用实参替换请求路径替换符
     */
    class Path(
        private val index: Int,
        private val method: Method,
        var name: String,
        private val encoded: Boolean
    ) : ParameterHandler<Any> {
        override fun apply(builder: Request.Builder, value: Any?) {
            val path = value ?: TubeUtils.parameterError(
                index,
                method,
                "Path $name value must not be null!"
            )
            builder.addPathParam(name, path.toString(), encoded)
        }
    }
}