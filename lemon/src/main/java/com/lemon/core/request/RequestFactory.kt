package com.lemon.core.request

import com.lemon.core.*
import com.lemon.core.appendPath
import com.lemon.core.isHttpProtocol
import com.lemon.core.request.body.FormBody
import com.lemon.core.request.body.MultipartBody
import com.lemon.core.request.body.RequestBody
import java.io.File
import java.lang.reflect.Method
import java.net.URLEncoder
import java.util.regex.Pattern

/**
 * Describe: 请求实体
 * Created by liya.zhu on 2022/3/3
 */
class RequestFactory constructor(
    private val originService: Class<*>,
    private val originMethod: Method,
    private val apiUrl: String,
    private val httpMethod: HttpMethod,
    private val serviceUrlPath: String,
    private var relativePath: String,
    private val headersBuilder: Headers.Builder,
) {

    private var body: RequestBody? = null
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
    fun addPathParam(name: String, value: String, encode: Boolean) = apply {
        val encodedValue = if (encode) {
            URLEncoder.encode(value, ContentType.FROM.getCharset().name())
        } else {
            value
        }
        val newUrlPath = relativePath.replace("{$name}", encodedValue)
        if (PATH_TRAVERSAL.matcher(newUrlPath).matches()) {
            throw IllegalArgumentException("@ApiPath parameters shouldn't perform path traversal ('.' or '..'):$value")
        }

        relativePath = newUrlPath
    }

    /**
     * 添加表单字段
     */
    fun addFormField(name: String, value: String, encode: Boolean) = apply {
        if (formBuilder == null) {
            formBuilder = FormBody.Builder()
        }
        formBuilder?.add(name, value, encode)
    }

    /**
     * 添加文件部件
     */
    fun addPart(name: String, encoding: String, part: Any) = apply {
        if (multipartBuilder == null) {
            multipartBuilder = MultipartBody.Builder()
        }

        when (part) {
            is MultipartBody.Part -> multipartBuilder?.addPart(part)
            is File -> multipartBuilder?.addPart(name, encoding, file = part)
            is RequestBody -> multipartBuilder?.addPart(name, encoding, part)
            else -> multipartBuilder?.addPart(name, encoding, part.toString())
        }
    }

    /**
     * 设置请求消息体
     */
    fun setBody(body: RequestBody?) = apply { this.body = body }

    /**
     * Api Url 追加 Get 请求参数
     */
    private fun appendQueryParams(relativeUrl: String, formBuilder: FormBody.Builder): String {
        val query = formBuilder.build().convertToQuery()
        return when (relativeUrl.indexOf("?")) {
            -1 -> {//未找到问号
                "$relativeUrl?$query"
            }
            relativeUrl.lastIndex -> {//问号在最后一个字符
                "$relativeUrl$query"
            }
            else -> {//问号后还有其它参数
                "$relativeUrl&$query"
            }
        }
    }

    fun build(): Request {

        var relativeUrl = when {
            relativePath.isHttpProtocol() -> {
                relativePath
            }
            serviceUrlPath.isHttpProtocol() -> {
                serviceUrlPath.appendPath(relativePath)
            }
            else -> {
                apiUrl.appendPath(serviceUrlPath, relativePath)
            }
        }

        if (body == null) {
            when {
                formBuilder != null -> {
                    if (httpMethod == HttpMethod.GET) {
                        relativeUrl = appendQueryParams(relativeUrl, formBuilder!!)
                    } else {
                        body = formBuilder!!.build()
                    }
                }
                multipartBuilder != null -> {
                    body = multipartBuilder!!.build()
                }
                httpMethod == HttpMethod.POST -> {
                    body = RequestBody.EMPTY_BODY
                }
            }
        }

        return Request.Builder(
            originService,
            originMethod,
            relativeUrl,
            httpMethod,
            headersBuilder,
            body
        ).build()
    }
}