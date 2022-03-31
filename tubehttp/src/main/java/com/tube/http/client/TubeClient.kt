package com.tube.http.client

import com.tube.http.HttpException
import com.tube.http.TubeUtils
import com.tube.http.request.Headers
import com.tube.http.request.Request
import com.tube.http.request.Response
import com.tube.http.request.body.ResponseBody
import com.tube.http.toURL
import com.tube.http.tryClose
import java.io.IOException
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.Proxy
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSocketFactory

/**
 * Describe: Tube Http 客户端
 * 默认客户端使用 HttpURLConnection
 * Created by liya.zhu on 2022/3/3
 */
class TubeClient private constructor(
    private val readTimeout: Int,
    private val connectTimeout: Int,
    private val proxy: Proxy?,
    private val sslSocketFactory: SSLSocketFactory,
    private val hostnameVerifier: HostnameVerifier,
) : HttpClient {

    override fun execute(request: Request): Response {
        try {
            return call(request)
        } catch (e: MalformedURLException) {
            throw HttpException.create("Url is not absolute:${request.url}", e)
        }
    }

    override fun readTimeout() = readTimeout

    override fun connectTimeout() = connectTimeout

    override fun proxy() = proxy

    /**
     * 发起请求调用
     */
    private fun call(request: Request): Response {
        val connection = openConnection(request)

        var ops: OutputStream? = null

        try {

            addHeaderPropertys(connection, request)

            if (request.alllowBody()) {
                request.body?.let {
                    val contentLength = it.contentLength()
                    if (contentLength == -1L) {
                        //Body 长度未知，采用默认分块大小传输
                        connection.setChunkedStreamingMode(0)
                    } else {
                        //设置 Body 已知长度
                        connection.setFixedLengthStreamingMode(contentLength)
                    }

                    ops = connection.outputStream.apply {
                        it.writeTo(this)
                        flush()
                    }
                }
            }

            connection.connect()

            val code = connection.responseCode
            val headers = parseResponseHeaders(connection)
            var body: ResponseBody? = null

            if (code in 200..299) {
                if (code !in 204..205) {
                    body = ResponseBody.create(
                        connection.inputStream.readBytes(),
                        headers.getContentType()
                    )
                }
            }

            return Response.Builder()
                .setRequest(request)
                .setCode(code)
                .setHeaders(headers)
                .setBody(body)
                .build()
        } catch (e: IOException) {
            throw HttpException.create("Execute request exception to ${request.url}", e)
        } finally {
            ops?.tryClose()
            connection.disconnect()
        }
    }

    /**
     * 创建 HttpURLConnection 连接
     */
    private fun openConnection(request: Request): HttpURLConnection {
        val url = request.url.toURL()

        val connection = try {
            if (proxy() != null) url.openConnection(proxy()) else url.openConnection()
        } catch (e: IOException) {
            throw HttpException.create("Unable to open connection to ${request.url}", e)
        } as HttpURLConnection

        connection.readTimeout = readTimeout()
        connection.connectTimeout = connectTimeout()
        connection.useCaches = false //不允许缓存
        connection.requestMethod = request.httpMethod
        connection.doInput = true
        connection.doOutput = request.alllowBody()

        if (connection is HttpsURLConnection) {
            connection.sslSocketFactory = sslSocketFactory
            connection.hostnameVerifier = hostnameVerifier
        }

        return connection
    }

    /**
     * 添加请求头参数
     */
    private fun addHeaderPropertys(httpConection: HttpURLConnection, request: Request) {
        val requestHeaders = request.headers.getRequestHeaders()
        for (entry in requestHeaders.entries) {
            httpConection.addRequestProperty(entry.key, entry.value)
        }
    }

    /**
     * 解析响应结果请求头
     */
    private fun parseResponseHeaders(
        httpConection: HttpURLConnection
    ): Headers {
        val headersFields = httpConection.headerFields ?: mapOf<String, List<String>>()
        return Headers.Builder().set(headersFields).build()
    }

    class Builder {
        private var readTimeout: Int = 60 * 1000 //读取超时时间，默认：60秒
        private var connectTimeout: Int = 60 * 1000 //连接超时时间，默认：60秒
        private var sslSocketFactory = TubeUtils.getDefaultSSLSocketFactory()// SslSocketFactory
        private var hostnameVerifier = TubeUtils.getDefaultHostnameVerifier()// HostnameVerifier
        private var proxy: Proxy? = null// 网络代理

        fun setReadTimeout(readTimeout: Int) = apply { this.readTimeout = readTimeout }

        fun setConnectTimeout(connectTimeout: Int) = apply { this.connectTimeout = connectTimeout }

        fun setProxy(proxy: Proxy) = apply { this.proxy = proxy }

        fun build() =
            TubeClient(readTimeout, connectTimeout, proxy, sslSocketFactory, hostnameVerifier)

    }

}