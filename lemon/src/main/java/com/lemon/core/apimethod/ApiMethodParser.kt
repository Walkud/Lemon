package com.lemon.core.apimethod

import com.lemon.core.*
import com.lemon.core.apimethod.parameter.ParameterHandler
import com.lemon.core.converter.Converter
import com.lemon.core.request.HttpMethod
import com.lemon.core.request.Request
import com.lemon.core.request.body.RequestBody
import java.lang.reflect.Method

/**
 * Describe: Api 接口与目标方法解析器
 * 1、解析 Api 接口类注解
 * 2、解析目标方法注解
 * 3、解析目标方法形参注解
 * 4、解析目标方法返回类型
 * Created by liya.zhu on 2022/3/4
 */
internal class ApiMethodParser(
    val lemon: Lemon,
    val originService: Class<*>,
    val originMethod: Method
) {
    private var apiUrl = ""
    private var relativePath = ""
    private var httpMethod = HttpMethod.POST //默认使用 POST
    private val headersBuilder = com.lemon.core.request.Headers.Builder()
    private val parameterHandlers = mutableListOf<ParameterHandler<*>>()
    private var isMultipart = false
    private var multiApiBody = false//是否使用多个 @ApiBody
    private var hasApiField = false//是否使用 @ApiField

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
                lemon.apiUrl,
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
        val genericReturnType = originMethod.genericReturnType
        val apiAdapter = lemon.apiAdapterFinder.findApiAdapter(genericReturnType, originMethod)
        val actualType = apiAdapter.getActualType(genericReturnType)
        val converter = lemon.converterFinder.findResponseBodyConverter(actualType, originMethod)
        return ApiMethod(lemon, this, converter, apiAdapter)
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

        if (isMultipart && httpMethod != HttpMethod.POST) {
            throw IllegalArgumentException(
                "When @Api's isMultipart property is set to true, the httpMethod property must be POST!" +
                        "for method:${originMethod.referenceName()}"
            )
        }

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
                    if (isMultipart) {
                        throw  IllegalArgumentException(
                            "@ApiBody cannot be used in conjunction with the isMultipart attribute of the @Api!" +
                                    "for method:${originMethod.referenceName()},typeName:$type"
                        )
                    }

                    if (hasApiField) {
                        throw  IllegalArgumentException(
                            "@ApiBody cannot be used with an @ApiField!" +
                                    "for method:${originMethod.referenceName()},typeName:$type"
                        )
                    }

                    if (multiApiBody) {
                        throw  IllegalArgumentException(
                            "Only one @ApiBody can be used for a method!" +
                                    "for method:${originMethod.referenceName()},typeName:$type"
                        )
                    }
                    multiApiBody = true
                    val converter: Converter<*, RequestBody> =
                        lemon.converterFinder.findRequestBodyConverter(type, originMethod)
                    ParameterHandler.Body(index, originMethod, converter)
                }
                is ApiField -> {
                    if (multiApiBody) {
                        throw  IllegalArgumentException(
                            "@ApiField cannot be used with an @ApiBody!" +
                                    "for method:${originMethod.referenceName()},typeName:$type"
                        )
                    }

                    if (isMultipart) {
                        throw  IllegalArgumentException(
                            "@Api cannot use @ApiField when the isMultipart property is set to true, use @ApiPart instead!" +
                                    "for method:${originMethod.referenceName()},typeName:$type"
                        )
                    }

                    hasApiField = true
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
                            "@ApiPart use you must set @Api's isMultipart property to true！" +
                                    "for method:${originMethod.referenceName()},typeName:$type"
                        )
                    }

                    if (multiApiBody) {
                        throw  IllegalArgumentException(
                            "The @ApiBody annotation cannot be used when the @Api isMultipart property is set to true！" +
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