package com.tube.http.apimethod

import com.tube.http.*
import com.tube.http.apimethod.parameter.ParameterHandler
import com.tube.http.converter.Converter
import com.tube.http.disposer.Disposer
import com.tube.http.request.HttpMethod
import com.tube.http.request.Request
import com.tube.http.request.body.RequestBody
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

/**
 * Describe: Api 接口与目标方法解析器
 * 1、解析 Api 接口类注解
 * 2、解析目标方法注解
 * 3、解析目标方法形参注解
 * 4、解析目标方法返回类型
 * Created by liya.zhu on 2022/3/4
 */
class ApiMethodParser(
    val tube: Tube,
    val originService: Class<*>,
    val originMethod: Method
) {
    private var apiUrl = ""
    private var relativePath = ""
    private var httpMethod = HttpMethod.POST //默认使用 POST
    private val headersBuilder = com.tube.http.request.Headers.Builder()
    private val parameterHandlers = mutableListOf<ParameterHandler<*>>()
    private var isMultipart = false

    init {
        //解析 Api 接口类注解
        parseClassAnnotation()
        //解析目标方法注解
        parseMethodAnnotation()
        //解析目标方法形参注解
        parseParameter()
    }

    /**
     * 根据目标方法实参构建请求类
     */
    fun buildRequest(args: Array<Any?>): Request {

        val reuqestBuilder =
            Request.Builder(
                originService,
                originMethod,
                tube.apiUrl,
                httpMethod,
                apiUrl,
                relativePath,
                headersBuilder,
                isMultipart,
            )

        val handlers = parameterHandlers as MutableList<ParameterHandler<Any>>
        for (i in handlers.indices) {
            handlers[i].apply(reuqestBuilder, args[i])
        }

        return reuqestBuilder.build()
    }

    /**
     * 获取 ApiMethod
     */
    fun getApiMethod(): ApiMethod {
        val returnType = originMethod.returnType
        var converterType = originMethod.genericReturnType
        val isDisposer = Disposer::class.java.isAssignableFrom(returnType)
        if (isDisposer) {
            if (converterType is ParameterizedType) {
                converterType = converterType.actualTypeArguments[0]
            } else {
                throw IllegalArgumentException(
                    "Disposer generic types must be defined!" +
                            "for method:${originMethod.referenceName()},returnTypeName:$converterType"
                )
            }
        }

        val converter = tube.converterFinder.findResponseBodyConverter(converterType, originMethod)
        return ApiMethod(tube, this, converter, isDisposer)
    }

    /**
     * 解析 Api 接口类注解
     */
    private fun parseClassAnnotation() {
        val classAnnotations = originService.annotations
        for (annotation in classAnnotations) {
            when (annotation) {
                is ApiUrl -> {
                    if (apiUrl.isEmpty()) {
                        apiUrl = annotation.value
                    } else {
                        throw  IllegalArgumentException(
                            "Only one @ApiUrl annotation can be used!" +
                                    "for class:${originService.name}"
                        )
                    }
                }
            }
        }
    }

    /**
     * 解析目标方法注解
     */
    private fun parseMethodAnnotation() {

        val methodAnnotations = originMethod.annotations
        for (annotation in methodAnnotations) {
            when (annotation) {
                is Api -> parseApiAnotation(annotation)
            }
        }
    }

    /**
     * 解析请求Path、方法、请求头等
     */
    private fun parseApiAnotation(annotation: Api) {
        this.relativePath = annotation.value
        this.httpMethod = annotation.method
        this.isMultipart = annotation.isMultipart
        val headers = annotation.headers
        for (header in headers) {
            val index = header.trim().indexOf(":")
            if (index < 3 || index == header.length - 1) {
                throw IllegalArgumentException(
                    "Check @Api headers format for $header!" +
                            "for method:${originMethod.referenceName()}"
                )
            }

            val headerName = header.substring(0, index).trim()
            val headerValue = header.substring(index + 1).trim()
            headersBuilder.add(headerName, headerValue)
        }
    }

    /**
     * 解析目标方法形参注解
     */
    private fun parseParameter() {

        val parameterTypes = originMethod.genericParameterTypes
        val parameterAnnotations = originMethod.parameterAnnotations

        for (index in parameterAnnotations.indices) {
            val type = parameterTypes[index]
            val annotations = parameterAnnotations[index]

            if (annotations.isEmpty() || annotations.size > 1) {
                throw  IllegalArgumentException(
                    "Method parameters have one and only one annotation" +
                            "for method:${originMethod.referenceName()},typeName:$type"
                )
            }

            val parameterHandler = when (val annotation = annotations.first()) {
                is ApiBody -> {
                    val converter: Converter<*, RequestBody> =
                        tube.converterFinder.findRequestBodyConverter(type, originMethod)
                    ParameterHandler.Body(index, originMethod, converter)
                }
                is ApiField -> {
                    val name = annotation.value
                    val encoded = annotation.encoded

                    if (type.isMapParameterizedType()) {
                        if (name.isNotEmpty()) {
                            throw  IllegalArgumentException(
                                "Map type @ApiField annotation value must be empty!" +
                                        "for method:${originMethod.referenceName()},typeName:$type"
                            )
                        }

                        if (type.isInvalidGenericParameterType()) {
                            throw  IllegalArgumentException(
                                "@ApiField Map generic types must be defined (e.g., Map<String, String>)!" +
                                        "for method:${originMethod.referenceName()},typeName:$type"
                            )
                        }

                        ParameterHandler.FieldMap(index, originMethod, encoded)
                    } else {
                        if (name.isEmpty()) {
                            throw  IllegalArgumentException(
                                "@ApiField annotation value cannot be empty!" +
                                        "for method:${originMethod.referenceName()},typeName:$type"
                            )
                        }
                        val rawType = type.asRawType()
                        if (rawType.isInvalidParameterType()) {
                            throw  IllegalArgumentException(
                                "@ApiField parameter type is invalid!" +
                                        "for method:${originMethod.referenceName()},typeName:$type"
                            )
                        }

                        ParameterHandler.Field(index, originMethod, name, encoded)
                    }
                }
                is ApiHeader -> {
                    val name = annotation.value
                    if (type.isMapParameterizedType()) {
                        if (name.isNotEmpty()) {
                            throw  IllegalArgumentException(
                                "Map type @ApiHeader annotation value must be empty!" +
                                        "for method:${originMethod.referenceName()},typeName:$type"
                            )
                        }

                        if (type.isInvalidGenericParameterType()) {
                            throw  IllegalArgumentException(
                                "@ApiHeader Map generic types must be defined (e.g., Map<String, String>)!" +
                                        "for method:${originMethod.referenceName()},typeName:$type"
                            )
                        }

                        ParameterHandler.HeaderMap(index, originMethod)
                    } else {
                        if (name.isEmpty()) {
                            throw  IllegalArgumentException(
                                "@ApiHeader annotation value cannot be empty!" +
                                        "for method:${originMethod.referenceName()},typeName:$type"
                            )
                        }

                        val rawType = type.asRawType()
                        if (rawType.isInvalidParameterType()) {
                            throw  IllegalArgumentException(
                                "@ApiHeader parameter type is invalid!" +
                                        "for method:${originMethod.referenceName()},typeName:$type"
                            )
                        }

                        ParameterHandler.Header(index, originMethod, name)
                    }
                }
                is ApiPath -> {
                    val rawType = type.asRawType()
                    if (rawType.isInvalidParameterType()) {
                        throw  IllegalArgumentException(
                            "@ApiPath parameter type is invalid!" +
                                    "for method:${originMethod.referenceName()},typeName:$type"
                        )
                    }
                    val name = annotation.value
                    val encoded = annotation.encoded
                    ParameterHandler.Path(index, originMethod, name, encoded)
                }
                is ApiPart -> {
                    if (!isMultipart) {
                        throw  IllegalArgumentException(
                            "@ApiPart annotation must be used with the @Multipart annotation！" +
                                    "for method:${originMethod.referenceName()},typeName:$type"
                        )
                    }

                    val name = annotation.value
                    val encoding = annotation.encoding
                    val rawType = type.asRawType()
                    when {
                        type.isMapParameterizedType() -> {
                            if (name.isNotEmpty()) {
                                throw  IllegalArgumentException(
                                    "Map type @ApiPart annotation value must be empty!" +
                                            "for method:${originMethod.referenceName()},typeName:$type"
                                )
                            }

                            if (type.isInvalidGenericParameterType()) {
                                throw  IllegalArgumentException(
                                    "@ApiPart Map generic types must be defined (e.g., Map<String, File>)!" +
                                            "for method:${originMethod.referenceName()},typeName:$type"
                                )
                            }

                            ParameterHandler.PartMap(index, originMethod, encoding)
                        }
                        rawType.isPartType() -> {
                            if (name.isNotEmpty()) {
                                throw  IllegalArgumentException(
                                    "MultipartBody.Part type @ApiPart annotation value must be empty!" +
                                            "for method:${originMethod.referenceName()},typeName:$type"
                                )
                            }

                            ParameterHandler.Part(index, originMethod, name, encoding)
                        }
                        else -> {
                            if (rawType.isInvalidParameterType()) {
                                throw  IllegalArgumentException(
                                    "@ApiPart parameter type is invalid!" +
                                            "for method:${originMethod.referenceName()},typeName:$type"
                                )
                            }

                            if (name.isEmpty()) {
                                throw  IllegalArgumentException(
                                    "@ApiPart value is empty, the parameter type must be MultipartBody.Part!" +
                                            "for method:${originMethod.referenceName()},typeName:$type"
                                )
                            }

                            ParameterHandler.Part(index, originMethod, name, encoding)
                        }
                    }
                }
                else -> {
                    null
                }
            }

            parameterHandler?.let {
                parameterHandlers.add(it)
            }
        }
    }
}