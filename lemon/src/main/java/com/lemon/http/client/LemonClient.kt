package com.lemon.http.client

import com.lemon.http.HttpException
import com.lemon.http.LemonUtils
import com.lemon.http.request.Headers
import com.lemon.http.request.Request
import com.lemon.http.request.Response
import com.lemon.http.request.body.ResponseBody
import com.lemon.http.toURL
import com.lemon.http.tryClose
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.Proxy
import java.net.SocketTimeoutException
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSocketFactory

/**
 * Describe: Lemon Http 客户端
 * 默认客户端使用 HttpURLConnection
 * Created by liya.zhu on 2022/3/3
 */
class LemonClient private constructor(
    private val readTimeout: Int,
    private val connectTimeout: Int,
    private val proxy: Proxy?,
    private val sslSocketFactory: SSLSocketFactory,
    private val hostnameVerifier: HostnameVerifier,
) : HttpClient {

    override fun execute(request: Request): Response {
        val finalRequest = HttpClient.addDefaultHeaders(request)
        return call(finalRequest)
    }

    override fun readTimeout() = readTimeout

    override fun connectTimeout() = connectTimeout

    override fun proxy() = proxy

    /**
     * 发起请求调用
     */
    private fun call(request: Request): Response {
        var connection: HttpURLConnection? = null
        var ops: OutputStream? = null
        var inps: InputStream? = null

        try {
            connection = openConnection(request)

            addHeaderPropertys(connection, request)

            try {
                connection.connect()
            } catch (e: Exception) {
                throw HttpException.connect(request.url, e)
            }

            if (request.hasBody()) {
                request.body?.let {
                    try {
                        ops = connection.outputStream.apply {
                            it.writeTo(this)
                            flush()
                        }
                    } catch (e: IOException) {
                        throw HttpException.write(request.url, e)
                    }
                }
            }

            val code = try {
                connection.responseCode
            } catch (e: IOException) {
                throw HttpException.code(-1, request.url)
            }

            val headers = parseResponseHeaders(connection)
            var body: ResponseBody? = null

            if (code in 200..299) {
                body = if (code in 204..205) {
                    ResponseBody.EMPTY_BODY
                } else {
                    try {
                        inps = connection.inputStream
                        ResponseBody.create(
                            connection.inputStream.readBytes(),
                            headers.getContentType()
                        )
                    } catch (e: IOException) {
                        throw HttpException.read(request.url, e)
                    }
                }
            }

            return Response.Builder()
                .setRequest(request)
                .setCode(code)
                .setHeaders(headers)
                .setBody(body)
                .build()
        } catch (e: SocketTimeoutException) {
            throw HttpException.timeOut(request.url, e)
        } catch (e: MalformedURLException) {
            throw HttpException.urlParse(request.url, e)
        } catch (e: HttpException) {
            throw e
        } catch (e: Exception) {
            throw HttpException.unknown(request.url, e)
        } finally {
            inps?.tryClose()
            ops?.tryClose()
            connection?.disconnect()
        }
    }

    /**
     * 创建 HttpURLConnection 连接并设置相关配置
     */
    private fun openConnection(request: Request): HttpURLConnection {
        val url = request.url.toURL()

        val connection = try {
            if (proxy() != null) url.openConnection(proxy()) else url.openConnection()
        } catch (e: IOException) {
            throw HttpException.openConnect(request.url, e)
        } as HttpURLConnection

        val hasBody = request.hasBody()
        connection.readTimeout = readTimeout()//设置读取超时时间
        connection.connectTimeout = connectTimeout()//设置连接超时时间
        connection.useCaches = false //不允许缓存
        connection.requestMethod = request.httpMethod.name //设置 Http 请求方式
        connection.doInput = true
        connection.doOutput = hasBody

        if (connection is HttpsURLConnection) {
            connection.sslSocketFactory = sslSocketFactory
            connection.hostnameVerifier = hostnameVerifier
        }

        if (hasBody) {
            request.body?.let {
                val contentLength = it.contentLength()
                if (contentLength == -1L) {
                    //Body 长度未知，采用默认分块大小传输
                    connection.setChunkedStreamingMode(0)
                } else {
                    //设置 Body 已知长度
                    connection.setFixedLengthStreamingMode(contentLength)
                }
            }
        }

        return connection
    }

    /**
     * 添加请求头参数
     */
    private fun addHeaderPropertys(conection: HttpURLConnection, request: Request) {
        val requestHeaders = request.headers.getRequestHeaders()
        for (entry in requestHeaders.entries) {
            conection.setRequestProperty(entry.key, entry.value)
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
        private var sslSocketFactory = LemonUtils.getDefaultSSLSocketFactory()// SslSocketFactory
        private var hostnameVerifier = LemonUtils.getDefaultHostnameVerifier()// HostnameVerifier
        private var proxy: Proxy? = null// 网络代理

        fun setReadTimeout(readTimeout: Int) = apply { this.readTimeout = readTimeout }

        fun setConnectTimeout(connectTimeout: Int) = apply { this.connectTimeout = connectTimeout }

        fun setProxy(proxy: Proxy) = apply { this.proxy = proxy }

        fun build() =
            LemonClient(readTimeout, connectTimeout, proxy, sslSocketFactory, hostnameVerifier)

    }

}