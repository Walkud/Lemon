package com.tube.http.log

/**
 * Describe: Tube 请求日志等级
 * Created by liya.zhu on 2022/4/14
 */
enum class TubeLogLevel(val value: Int) {
    /**
     * 无日志
     */
    NONE(0),

    /**
     * 经典日志，仅显示请求发起和响应消息。
     * 例如：
     * --> POST https://xxx.xxx.xxx/yyy (10 byte body)
     * <-- POST 200 OK (xx ms, x byte body)
     */
    BASIC(1),

    /**
     * 带请求头日志
     * 例如：
     * --> POST https://xxx.xxx.xxx/yyy
     * HEADERS CONTENT:
     * Content-Type: plain/text
     * Content-Length: 10
     * ...
     * <-- POST 200 OK (xx ms, x byte body)
     */
    HEADERS(2),
    BODY(3),
    ALL(4);
}