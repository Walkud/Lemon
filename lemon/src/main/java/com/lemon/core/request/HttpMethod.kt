package com.lemon.core.request

/**
 * Describe: Http 请求方式
 * Created by liya.zhu on 2022/4/11
 */
enum class HttpMethod {
    /**
     * 常用
     * 用于获取资源，参数会追加在 url 的 '?' 后提交
     * safe and Idempotent：安全、幂等
     */
    GET,

    /**
     * 常用
     * 用于创建资源，参数会以消息体方式提交
     * safe and Idempotent：非安全、非幂等
     */
    POST,

    /**
     * 一般
     * 用于获取资源，仅获取资源的部分信息(例如：Content-Length、Content-Type)
     * safe and Idempotent：安全、幂等
     */
    HEAD,

    /**
     * 一般
     * 用于创建、更新资源
     * safe and Idempotent：非安全、幂等
     */
    PUT,

    /**
     * 一般
     * 用于创建、更新部分资源，使用时需要判断服务端是否支持
     * safe and Idempotent：非安全、幂等
     */
    PATCH,

    /**
     * 一般
     * 用于删除资源
     * safe and Idempotent：非安全、幂等
     */
    DELETE,

    /**
     * 一般
     * 用于 Url 验证接口服务是否正常
     * safe and Idempotent：安全、幂等
     */
    OPTIONS;

//    /**
//     * 少见，支持该方式的服务器存在跨站脚本漏洞，这里禁用此方式
//     * 回显服务器收到的请求参数
//     * safe and Idempotent：安全、幂等
//     */
//    TRACE

    /**
     * 是否有Body
     */
    fun hasBody(): Boolean {
        return when (this) {
            POST, PUT, PATCH -> true
            else -> false
        }
    }
}