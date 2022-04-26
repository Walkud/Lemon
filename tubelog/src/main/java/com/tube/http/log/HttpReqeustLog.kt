package com.tube.http.log

import com.tube.http.client.HttpClient
import com.tube.http.request.Request
import com.tube.http.request.Response
import com.tube.http.request.body.FormBody
import com.tube.http.request.body.MultipartBody
import com.tube.http.request.body.RequestBody
import java.io.ByteArrayOutputStream
import java.lang.StringBuilder

/**
 * Describe: Http 日志实体类
 * Created by liya.zhu on 2022/4/14
 */
class HttpReqeustLog(val level: TubeLogLevel) {

    var request: Request? = null
    var response: Response? = null

    /**
     * 获取请求日志
     * 1、构建新的 Request
     * 2、构建请求日志内容
     * @param request 原请求
     * @return 请求日志内容
     */
    fun getRequestLog(request: Request): String {
        var bodyContent: String? = null
        //构建新的 Request，并返回请求消息体内容
        val builder = request.newBuilder().setBody(null)
        request.body?.let { body ->
            val bodyByteArray = ByteArrayOutputStream().let { baops ->
                body.writeTo(baops)
                baops.toByteArray()
            }

            val charset = body.contentType()?.getCharset() ?: Charsets.UTF_8
            bodyContent = String(bodyByteArray, charset)

            //重新构建 Body,防止内部引用导致内存泄漏
            val newRequestBody = when (body) {
                is FormBody -> body.newBuilder().build()
                is MultipartBody -> body.newBuilder().build()
                else -> RequestBody.Companion.create(bodyByteArray, body.contentType())
            }
            builder.setBody(newRequestBody)
        }

        val finalRequest = HttpClient.addDefaultHeaders(builder.build()).also { newRequest ->
            this.request = newRequest
        }

        return StringBuilder().apply {
            val methodName = finalRequest.httpMethod.name
            append("Request ")
            append(methodName)
            append(" ")
            append(finalRequest.url)
            append(" ")

            if (level.value >= TubeLogLevel.ALL.value) {
                //构建 ApiService 相关信息
                append("\n")
                append("APISERVICE:\n")
                append("ApiServiceClassName:")
                append(finalRequest.originService.name)
                append("\n")
                append("ApiServiceMehodName:")
                append(finalRequest.originMethod.name)
            }

            if (level.value >= TubeLogLevel.HEADERS.value) {
                //构建请求头信息
                append("\n")
                val headersMap = finalRequest.headers.getRequestHeaders()
                append("HEADERS:\n")
                for (key in headersMap.keys) {
                    append(key)
                    append(":")
                    append(headersMap[key])
                    append("\n")
                }
            }

            val bodyLengthStr = finalRequest.body?.let {
                "(${it.contentLength()} byte body)"
            } ?: ""

            bodyContent?.let {
                if (level.value >= TubeLogLevel.BODY.value) {
                    append("BODY:\n")
                    append(bodyContent)
                }
                append("\n")
            }

            append("Request ")
            append(methodName)
            append(" ")
            append("END ")
            append(bodyLengthStr)


        }.toString()
    }

    /**
     * 获取请求响应日志
     */
    fun getResponseLog(response: Response, time: Long): String {
        this.response = response
        return StringBuilder().apply {
            val request = response.request
            val methodName = request.httpMethod.name
            append("\n")
            append("Response ")
            append(methodName)
            append(" ")
            append(request.url)

            val responseBody = response.body
            val bodyLengthStr = responseBody.let {
                "($time ms,${it.contentLength()} byte body)"
            }

            if (level.value >= TubeLogLevel.HEADERS.value) {
                append("\n")
                val headersMap = response.headers.getRequestHeaders()
                append("HEADERS:\n")
                for (key in headersMap.keys) {
                    append(key)
                    append(":")
                    append(headersMap[key])
                    append("\n")
                }
            }

            if (level.value >= TubeLogLevel.BODY.value) {
                append("BODY:\n")
                response.body.let {
                    val charset = it.contentType()?.getCharset() ?: Charsets.UTF_8
                    append(String(it.byteArray(), charset))
                }
                append("\n")
            }

            append("Response ")
            append(methodName)
            append(" ")
            append("END ")
            append(bodyLengthStr)
            append("\n\n")
        }.toString()
    }
}