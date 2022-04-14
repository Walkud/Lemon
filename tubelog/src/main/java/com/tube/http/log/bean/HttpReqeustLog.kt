package com.tube.http.log.bean

import com.tube.http.log.TubeLogLevel
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
     */
    fun getRequestLog(request: Request): String {
        val requestBodyContent = buildNewRequest(request)
        return StringBuilder().apply {

            val methodName = request.httpMethod.name
            append("Request ")
            append(methodName)
            append(" ")
            append(request.url)
            append(" ")

            if (level.value >= TubeLogLevel.ALL.value) {
                //构建 ApiService 相关信息
                append("\n")
                append("APISERVICE:\n")
                append("ApiServiceClassName:")
                append(request.originService.name)
                append("\n")
                append("ApiServiceMehodName:")
                append(request.originMethod.name)
            }

            if (level.value >= TubeLogLevel.HEADERS.value) {
                //构建请求头信息
                append("\n")
                val headersMap = request.headers.getRequestHeaders()
                append("HEADERS:\n")
                for (key in headersMap.keys) {
                    append(key)
                    append(":")
                    append(headersMap[key])
                    append("\n")
                }
            }

            val requestBody = request.body
            val bodyLengthStr = requestBody?.let {
                "(${it.contentLength()} byte body)"
            } ?: ""

            if (level.value >= TubeLogLevel.BODY.value) {
                append("BODY:\n")
                append(requestBodyContent)
            }

            append("\n")
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
            append("Response ")
            append(methodName)
            append(" ")
            append(request.url)

            val requestBody = request.body
            val bodyLengthStr = requestBody?.let {
                "($time ms,${it.contentLength()} byte body)"
            } ?: ""

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
            }

            append("\n")
            append("Response ")
            append(methodName)
            append(" ")
            append("END ")
            append(bodyLengthStr)
        }.toString()
    }

    /**
     * 构建新的 Request，并返回请求消息体内容
     */
    private fun buildNewRequest(request: Request): String {
        var requestBodyContent = ""
        val builder = Request.Builder(
            request.originService,
            request.originMethod,
            request.apiUrl,
            request.httpMethod,
            request.urlPath,
            request.serviceUrlPath,
            request.headers.newBuilder(),
            request.isMultipart,
            null,
        )

        request.body?.let { body ->
            val bodyByteArray = ByteArrayOutputStream().let { baops ->
                body.writeTo(baops)
                baops.toByteArray()
            }

            val charset = body.contentType()?.getCharset() ?: Charsets.UTF_8
            requestBodyContent = String(bodyByteArray, charset)

            //重新构建 Body
            val newRequestBody = when (body) {
                is FormBody -> body.newBuilder().build()
                is MultipartBody -> body.newBuilder().build()
                else -> RequestBody.Companion.create(bodyByteArray, body.contentType())
            }
            builder.setBody(newRequestBody)
        }
        this.request = builder.build()

        return requestBodyContent
    }
}