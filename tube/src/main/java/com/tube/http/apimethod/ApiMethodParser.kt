package com.tube.http.apimethod

import com.tube.http.*
import com.tube.http.apimethod.parameter.ParameterHandler
import com.tube.http.converter.Converter
import com.tube.http.disposer.Disposer
import com.tube.http.request.Request
import com.tube.http.request.body.RequestBody
import com.tube.http.request.body.ResponseBody
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

    private var serviceUrlPath = ""
    private var urlPath = ""
    private var httpMethod = ""
    private val headersBuilder = com.tube.http.request.Headers.Builder()
    private val parameterHandlers = mutableListOf<ParameterHandler<*>>()
    private var isMultipart = false
    var responseConverter: Converter<ResponseBody, *>
        private set

    init {
        //解析 Api 接口类注解
        parseClassAnnotation()
        //解析目标方法注解
        parseMethodAnnotation()
        //解析目标方法形参注解
        parseParameter()
        //解析目标方法返回类型，并找到相应消息体转换器
        responseConverter = parseReturnType()
    }

    /**
     * 根据目标方法实参构建请求类
     */
    fun buildRequest(args: Array<Any?>): Request {

        val reuqestBuilder =
            Request.Builder(
                originService,
                originMethod,
                tube.baseUrl,
                httpMethod,
                serviceUrlPath,
                urlPath,
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
     * 解析 Api 接口类注解
     */
    private fun parseClassAnnotation() {
        val classAnnotations = originService.annotations
        for (annotation in classAnnotations) {
            when (annotation) {
                is BaseUrl -> {
                    if (serviceUrlPath.isEmpty()) {
                        serviceUrlPath = annotation.value
                    } else {
                        throw  IllegalArgumentException(
                            "Only one @BaseUrl annotation can be used!" +
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
                is GET -> parseHttpMethod("GET", annotation, annotation.value)
                is POST -> parseHttpMethod("POST", annotation, annotation.value)
                is Headers -> parseMethodHeader(annotation, annotation.value)
                is Multipart -> isMultipart = true
            }
        }
    }

    /**
     * 解析请求方法类型
     */
    private fun parseHttpMethod(httpMethod: String, annotation: Annotation, value: String) {
        if (this.httpMethod.isEmpty()) {
            this.httpMethod = httpMethod
            this.urlPath = value
        } else {
            throw  IllegalArgumentException(
                "Only one Http Method annotation can be used!" +
                        "for method:${originMethod.referenceName()}"
            )
        }
    }

    /**
     * 解析请求头
     */
    private fun parseMethodHeader(annotation: Annotation, values: Array<String>) {
        val annotationName = annotation::class.java.simpleName
        for (value in values) {
            val index = value.trim().indexOf(":")
            if (index < 3 || index == value.length - 1) {
                throw IllegalArgumentException("Check @$annotationName value format for $value!")
            }

            val headerName = value.substring(0, index).trim()
            val headerValue = value.substring(index + 1).trim()
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
                is Body -> {
                    val converter: Converter<*, RequestBody> =
                        tube.converterFinder.findRequestBodyConverter(type, originMethod)
                    ParameterHandler.Body(index, originMethod, converter)
                }
                is Field -> {
                    val rawType = type.asRawType()
                    if (rawType.isInvalidParameterType()) {
                        throw  IllegalArgumentException(
                            "@Field parameter type is invalid!" +
                                    "for method:${originMethod.referenceName()},typeName:$type"
                        )
                    }

                    val name = annotation.value
                    val encoded = annotation.encoded
                    ParameterHandler.Field(index, originMethod, name, encoded)
                }
                is FieldMap -> {
                    if (type.isNotMapParameterizedType()) {
                        throw  IllegalArgumentException(
                            "@FieldMap parameter type must be Map!" +
                                    "for method:${originMethod.referenceName()},typeName:$type"
                        )
                    }

                    if (type.isInvalidGenericParameterType()) {
                        throw  IllegalArgumentException(
                            "@FieldMap Map generic types must be defined (e.g., Map<String, String>)!" +
                                    "for method:${originMethod.referenceName()},typeName:$type"
                        )
                    }

                    val encoded = annotation.encoded

                    ParameterHandler.FieldMap(index, originMethod, encoded)
                }
                is Header -> {
                    val rawType = type.asRawType()
                    if (rawType.isInvalidParameterType()) {
                        throw  IllegalArgumentException(
                            "@Header parameter type is invalid!" +
                                    "for method:${originMethod.referenceName()},typeName:$type"
                        )
                    }
                    val name = annotation.value
                    ParameterHandler.Header(index, originMethod, name)
                }
                is HeaderMap -> {
                    if (type.isNotMapParameterizedType()) {
                        throw  IllegalArgumentException(
                            "@FieldMap parameter type must be Map!" +
                                    "for method:${originMethod.referenceName()},typeName:$type"
                        )
                    }

                    if (type.isInvalidGenericParameterType()) {
                        throw  IllegalArgumentException(
                            "@FieldMap Map generic types must be defined (e.g., Map<String, String>)!" +
                                    "for method:${originMethod.referenceName()},typeName:$type"
                        )
                    }

                    ParameterHandler.HeaderMap(index, originMethod)
                }
                is Path -> {
                    val rawType = type.asRawType()
                    if (rawType.isInvalidParameterType()) {
                        throw  IllegalArgumentException(
                            "@Path parameter type is invalid!" +
                                    "for method:${originMethod.referenceName()},typeName:$type"
                        )
                    }
                    val name = annotation.value
                    val encoded = annotation.encoded
                    ParameterHandler.Path(index, originMethod, name, encoded)
                }
                is Part -> {
                    if (!isMultipart) {
                        throw  IllegalArgumentException(
                            "@Part annotation must be used with the @Multipart annotation！" +
                                    "for method:${originMethod.referenceName()},typeName:$type"
                        )
                    }

                    if (type.isMapParameterizedType()) {
                        throw  IllegalArgumentException(
                            "@Part parameter does not support Map type.Use the @partMap annotation!" +
                                    "for method:${originMethod.referenceName()},typeName:$type"
                        )
                    }

                    val rawType = type.asRawType()
                    if (rawType.isInvalidParameterType()) {
                        throw  IllegalArgumentException(
                            "@Part parameter type is invalid!" +
                                    "for method:${originMethod.referenceName()},typeName:$type"
                        )
                    }

                    val name = annotation.value
                    if (name.isEmpty() && !rawType.isPartType()) {
                        throw  IllegalArgumentException(
                            "@Part value is empty, the parameter type must be MultipartBody.Part!" +
                                    "for method:${originMethod.referenceName()},typeName:$type"
                        )
                    }

                    val encoding = annotation.encoding
                    ParameterHandler.Part(index, originMethod, name, encoding)
                }
                is PartMap -> {
                    if (!isMultipart) {
                        throw  IllegalArgumentException(
                            "@PartMap annotation must be used with the @Multipart annotation！" +
                                    "for method:${originMethod.referenceName()},typeName:$type"
                        )
                    }

                    if (type.isNotMapParameterizedType()) {
                        throw  IllegalArgumentException(
                            "@PartMap parameter type must be Map!" +
                                    "for method:${originMethod.referenceName()},typeName:$type"
                        )
                    }

                    if (type.isInvalidGenericParameterType()) {
                        throw  IllegalArgumentException(
                            "@PartMap Map generic types must be defined (e.g., Map<String, File>)!" +
                                    "for method:${originMethod.referenceName()},typeName:$type"
                        )
                    }

                    val encoding = annotation.encoding
                    ParameterHandler.PartMap(index, originMethod, encoding)
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

    /**
     * 解析目标方法返回类型，并找到相应消息体转换器
     */
    private fun parseReturnType(): Converter<ResponseBody, *> {
        val returnType = originMethod.returnType

        if (!Disposer::class.java.isAssignableFrom(returnType)) {
            throw IllegalArgumentException(
                "Return type must be Disposer class!" +
                        "for method:${originMethod.referenceName()},returnTypeName:$returnType"
            )
        }

        val genericReturnType = originMethod.genericReturnType
        if (genericReturnType is ParameterizedType) {
            val types = genericReturnType.actualTypeArguments
            if (types.size == 1) {
                return tube.converterFinder.findResponseBodyConverter(types[0], originMethod)
            }
        }

        throw IllegalArgumentException(
            "Disposer generic types must be defined!" +
                    "for method:${originMethod.referenceName()},returnTypeName:$genericReturnType"
        )
    }
}