package com.lemon.log

import com.lemon.core.interceptor.Interceptor
import com.lemon.log.logger.DefaultLemonLogger
import com.lemon.core.request.Response

/**
 * Describe: Lemon Http 请求日志拦截器
 * 根据日志等级打印请求日志内容
 *
 * 注意：线上环境请移除该拦截器(建议)或将拦截器日志等级设置为 LemonLogLevel.NONE ，否则会可能会泄漏请求数据。
 * Created by liya.zhu on 2022/4/12
 */
class LemonLogInterceptor(
    var level: LemonLogLevel = LemonLogLevel.ALL,
    private val defaultLogger: DefaultLemonLogger = DefaultLemonLogger()
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (level == LemonLogLevel.NONE) {//无需输出请求日志
            return chain.proceed(request)
        }

        val httpReqeustLog = HttpReqeustLog(level)
        val requestLog = httpReqeustLog.getRequestLog(request)
        //输出请求日志
        defaultLogger.log(requestLog)

        val start = System.currentTimeMillis()
        val response = try {
            chain.proceed(request)
        } catch (t: Throwable) {
            //输出异常日志
            defaultLogger.log("Http Request Failed: $t")
            throw t
        }

        //计算执行耗时
        val time = System.currentTimeMillis() - start
        val responseLog = httpReqeustLog.getResponseLog(response, time)
        //输出请求响应日志
        defaultLogger.log(responseLog)
        return response
    }
}