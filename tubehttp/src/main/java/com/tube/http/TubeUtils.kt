package com.tube.http

import java.lang.reflect.*
import java.security.GeneralSecurityException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

/**
 * Describe: 工具类
 * Created by liya.zhu on 2022/3/3
 */
internal class TubeUtils private constructor() {

    companion object {

        val userAgent by lazy {
            "TubeHttp/${BuildConfig.VERSION_NAME}"
        }

        /**
         * 获取 32 位移除分隔符的随机UUID
         */
        fun getRandomUUID32(): String {
            return UUID.randomUUID().toString().replace("-", "")
        }

        /**
         * 检查 Class 是否为接口
         */
        fun checkServiceClass(service: Class<*>) {
            if (!service.isInterface) {
                throw RuntimeException("${service.name} must be interfaces.")
            }
        }

        /**
         * 抛出参数异常错误
         */
        @Throws(RuntimeException::class)
        fun parameterError(index: Int, method: Method, msg: String, throwable: Throwable? = null) {
            throw IllegalArgumentException(
                "$msg\n    for method ${method.referenceName()}", throwable
            )
        }

        /**
         * 获取默认 HostnameVerifier
         */
        fun getDefaultHostnameVerifier() = HostnameVerifier { _, _ -> true }

        /**
         * 获取默认的 SSLSocketFactory
         */
        fun getDefaultSSLSocketFactory(): SSLSocketFactory {
            try {
                val sslContext = SSLContext.getInstance("TLS")
                val x509trustmanager = object : X509TrustManager {
                    override fun checkClientTrusted(
                        chain: Array<out X509Certificate>?,
                        authType: String?
                    ) {
                    }

                    override fun checkServerTrusted(
                        chain: Array<out X509Certificate>?,
                        authType: String?
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return arrayOf()
                    }
                }
                sslContext.init(
                    null,
                    arrayOf(x509trustmanager),
                    SecureRandom()
                )
                return sslContext.socketFactory
            } catch (e: GeneralSecurityException) {
                throw RuntimeException(e)
            }
        }
    }
}