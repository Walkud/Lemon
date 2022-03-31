package com.tube.http.client

import com.tube.http.request.Request
import com.tube.http.request.Response
import java.io.IOException
import java.net.Proxy
import kotlin.jvm.Throws

/**
 * Describe: Http 请求客户端接口
 * Http 请求客户端可自定义，也可以使用默认 TubeClient
 * Created by liya.zhu on 2022/3/2
 */
interface HttpClient {

    @Throws(IOException::class)
    fun execute(request: Request): Response

    /**
     * 读取超时时间
     */
    fun readTimeout(): Int

    /**
     * 连接超时时间
     */
    fun connectTimeout(): Int

    /**
     * 网络代理
     */
    fun proxy(): Proxy?

}