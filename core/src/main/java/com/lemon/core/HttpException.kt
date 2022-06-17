package com.lemon.core

/**
 * Describe: Http 异常统一封装类
 * Created by liya.zhu on 2022/3/15
 */
open class HttpException : RuntimeException {
    val state: State

    constructor(state: State, message: String) : super(message) {
        this.state = state
    }

    constructor(state: State, message: String, cause: Throwable) : super(message, cause) {
        this.state = state
    }

    companion object {

        fun code(code: Int, url: String) = HttpException(State.CODE, "Http $code error! url:$url")

        fun read(url: String, cause: Throwable) =
            HttpException(State.READ, "Execute $url request read exception!", cause)

        fun urlParse(url: String, cause: Throwable) =
            HttpException(State.URL_PARSE, "Url is not absolute:$url", cause)

        fun openConnect(url: String, cause: Throwable) =
            HttpException(State.CONNECT, "Unable to open connection $url", cause)

        fun connect(url: String, cause: Throwable) =
            HttpException(State.CONNECT, "Execute $url request connect exception!", cause)

        fun write(url: String, cause: Throwable) =
            HttpException(State.WRITE, "Execute  $url request write exception!", cause)

        fun timeOut(url: String, cause: Throwable) =
            HttpException(State.TIME_OUT, "Execute $url request timeout exception!", cause)

        fun unknown(url: String, cause: Throwable) =
            HttpException(State.Unknown, "Execute $url request unknown exception!", cause)
    }

    enum class State {
        /**
         *  Http 响应 Code 异常状态，例如重定向、客服端参数错误、服务端异常都会归为此类状态
         *  使用 LemonClient 时 code 码为 -1 则表示读取 responseCode 异常
         */
        CODE,

        /**
         * Url 解析异常状态，例如不规范的请求 Url 归为此类异常
         */
        URL_PARSE,

        /**
         * 连接异常状态，例如服务端未启动,未知的地址发生的连接异常归为此类状态
         */
        CONNECT,

        /**
         * 写异常状态，例如 outputStream 写入时发生的 IO 异常归为此类状态
         */
        WRITE,

        /**
         * 读异常状态，例如 inputStream 读时发生的 IO 异常归为此类状态
         */
        READ,

        /**
         * 超时异常状态，例如：连接超时，读取超时异常归为此类状态
         */
        TIME_OUT,

        /**
         * 未知异常状态，例如：代码 Bug 引起的异常或未处理的异常归为此类状态
         */
        Unknown
    }
}