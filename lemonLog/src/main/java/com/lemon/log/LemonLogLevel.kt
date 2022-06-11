package com.lemon.log

/**
 * Describe: Lemon 请求日志等级
 * Created by liya.zhu on 2022/4/14
 */
enum class LemonLogLevel(val value: Int) {
    /**
     * 无日志
     */
    NONE(0),

    /**
     * 经典日志，仅显示请求发起和响应消息。
     * 例如：
     * Request POST https://xxx.xxx.xxx/yyy (10 byte body)
     * <-- POST 200 OK (xx ms, x byte body)
     */
    BASIC(1),

    /**
     * 带请求头日志
     * 例如：
     * Request POST https://xxx.xxx.xxx/yyy
     * HEADERS CONTENT:
     * Content-Type: plain/text
     * Content-Length: 10
     * ...
     * Response POST 200 OK (xx ms, x byte body)
     */
    HEADERS(2),

    /**
     * 带请求头、消息体日志
     * Request GET http://xxx.xxx.xxx/yyy
     * HEADERS:
     * X-CALL-ID:call123
     * X-Token:token123456
     * User-Agent:Lemon/0.1.0.1-SNAPSHOT
     * Host:xxx.xxx.xxx
     * Connection:Keep-Alive
     * Request GET END
     * Response GET http://xxx.xxx.xxx/yyy
     * HEADERS:
     * Keep-Alive:timeout=60
     * Transfer-Encoding:chunked
     * null:HTTP/1.1 200
     * Connection:keep-alive
     * Date:Tue, 24 May 2022 03:02:58 GMT
     * Content-Type:application/json
     * BODY:
     * {"code":0,"msg":"","data":{"time":1653361378403}}
     * Response GET END (727 ms,49 byte body)
     */
    BODY(3),

    /**
     * 带请求头、消息体、声明接口类及函数方法日志
     * Request GET http://xxx.xxx.xxx/yyy
     * APISERVICE:
     * ApiServiceClassName:com.lemon.core.KotlinApiService
     * ApiServiceMehodName:getServerTime
     * HEADERS:
     * X-CALL-ID:call123
     * X-Token:token123456
     * User-Agent:Lemon/0.1.0.1-SNAPSHOT
     * Host:xxx.xxx.xxx
     * Connection:Keep-Alive
     * Request GET END
     * Response GET http://xxx.xxx.xxx/yyy
     * HEADERS:
     * Keep-Alive:timeout=60
     * Transfer-Encoding:chunked
     * null:HTTP/1.1 200
     * Connection:keep-alive
     * Date:Tue, 24 May 2022 03:02:58 GMT
     * Content-Type:application/json
     * BODY:
     * {"code":0,"msg":"","data":{"time":1653361378403}}
     * Response GET END (727 ms,49 byte body)
     */
    ALL(4);
}