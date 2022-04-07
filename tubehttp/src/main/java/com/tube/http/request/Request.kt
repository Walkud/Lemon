package com.tube.http.request

import com.tube.http.*
import com.tube.http.appendPath
import com.tube.http.isGetMethod
import com.tube.http.isHttpProtocol
import com.tube.http.isPostMethod
import com.tube.http.request.body.FormBody
import com.tube.http.request.body.MultipartBody
import com.tube.http.request.body.RequestBody
import java.io.File
import java.lang.reflect.Method
import java.net.URLEncoder
import java.util.regex.Pattern

/**
 * Describe: 请求实体
 * Created by liya.zhu on 2022/3/3
 */
class Request private constructor(
    val originService: Class<*>,
    val originMethod: Method,
    val baseUrl: String,
    val httpMethod: String,
    val serviceUrlPath: String,
    val urlPath: String,
    val headers: Headers,
    val isMultipart: Boolean,
    val body: RequestBody?
) {
    /**
     * 请求Url
     */
    val url by lazy {
        getRequestUrl()
    }

    private fun getRequestUrl(): String {
        val relativeUrl = if (serviceUrlPath.isHttpProtocol()) {
            serviceUrlPath.appendPath(urlPath)
        } else {
            baseUrl.appendPath(serviceUrlPath, urlPath)
        }

        if (httpMethod.isGetMethod() && body is FormBody) {
            val query = body.convertToQuery()
            return when (relativeUrl.indexOf("?")) {
                -1 -> {
                    "$relativeUrl?$query"
                }
                relativeUrl.length - 1 -> {
                    "$relativeUrl$query"
                }
                else -> {
                    "$relativeUrl&$query"
                }
            }
        }

        return relativeUrl
    }

    /**
     * 是否允许 Body
     */
    fun alllowBody() = httpMethod.isPostMethod()

    fun newBuilder(): Builder {
        return Builder(
            originService,
            originMethod,
            baseUrl,
            httpMethod,
            serviceUrlPath,
            urlPath,
            headers.newBuilder(),
            isMultipart,
            body
        )
    }

    class Builder(
        private val originService: Class<*>,
        private val originMethod: Method,
        private val baseUrl: String,
        private val httpMethod: String,
        private val serviceUrlPath: String,
        private var urlPath: String,
        private val headersBuilder: Headers.Builder,
        private val isMultipart: Boolean,
        private var body: RequestBody? = null
    ) {

        private var formBuilder: FormBody.Builder? = null
        private var multipartBuilder: MultipartBody.Builder? = null

        companion object {
            private val PATH_TRAVERSAL = Pattern.compile("(.*/)?(\\.|%2e|%2E){1,2}(/.*)?")
        }

        /**
         * 设置请求头参数
         */
        fun setHeader(key: String, value: String) = apply { headersBuilder.set(key, value) }

        /**
         * 添加请求头参数
         */
        fun addHeader(key: String, value: String) = apply { headersBuilder.add(key, value) }

        /**
         * 移除请求头参数
         */
        fun removeHeader(key: String) = apply { headersBuilder.remove(key) }

        /**
         * 替换 Url Path 参数
         */
        fun addPathParam(name: String, value: String, encode: Boolean) {
            val encodedValue = if (encode) {
                URLEncoder.encode(value, ContentType.FROM.getCharset().name())
            } else {
                value
            }
            val newUrlPath = urlPath.replace("{$name}", encodedValue)
            if (PATH_TRAVERSAL.matcher(newUrlPath).matches()) {
                throw IllegalArgumentException("@Path parameters shouldn't perform path traversal ('.' or '..'):$value")
            }

            urlPath = newUrlPath
        }

        /**
         * 添加表单字段
         */
        fun addFormField(name: String, value: String, encode: Boolean) {
            if (formBuilder == null) {
                formBuilder = FormBody.Builder()
            }
            formBuilder?.add(name, value, encode)
        }

        /**
         * 添加文件部件
         */
        fun addPart(name: String, encoding: String, part: Any) {
            if (multipartBuilder == null) {
                multipartBuilder = MultipartBody.Builder()
            }

            when (part) {
                is MultipartBody.Part -> multipartBuilder?.addPart(part)
                is File -> multipartBuilder?.addPart(name, encoding, part)
                is RequestBody -> multipartBuilder?.addPart(name, encoding, part)
                else -> multipartBuilder?.addPart(name, encoding, part.toString())
            }
        }

        /**
         * 设置请求消息体
         */
        fun setBody(body: RequestBody?) = apply { this.body = body }

        fun build(): Request {

            if (body == null) {
                when {
                    formBuilder != null -> {
                        body = formBuilder!!.build()
                    }
                    multipartBuilder != null -> {
                        body = multipartBuilder!!.build()
                    }
                    httpMethod.isPostMethod() -> {
                        body = RequestBody.EMPTY_BODY
                    }
                }
            }

            return Request(
                originService,
                originMethod,
                baseUrl,
                httpMethod,
                serviceUrlPath,
                urlPath,
                headersBuilder.build(),
                isMultipart,
                body
            )
        }
    }
}