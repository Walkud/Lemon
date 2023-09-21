package com.lemon.core.request

/**
 * Describe: 请求头
 * Created by liya.zhu on 2022/3/16
 */
class Headers private constructor(
    private val headerMap: Map<String, List<String>>,
    private val contentType: ContentType
) {

    companion object {
        const val CONTENT_TYPE_KEY = "Content-Type"
        const val USER_AGENT_KEY = "User-Agent"
        const val HOST_KEY = "Host"
        const val CONNECTION_KEY = "Connection"
        const val CONTENT_LENGTH_KEY = "Content-Length"
        const val TRANSFER_ENCODING_KEY = "Transfer-Encoding"
        const val ACCEPT_ENCODING = "Accept-Encoding"
        const val RANGE = "Range"
        const val CONTENT_ENCODING = "Content-Encoding"
    }

    /**
     * 获取请求头参数值列表(忽略大小写)
     */
    fun get(key: String): List<String>? {
        for (entry in entries()) {
            if (key.equals(entry.key, true)) {
                return entry.value
            }
        }
        return null
    }

    /**
     * 根据 Key 获取请求头第一个参数
     */
    fun getFirst(key: String) = get(key)?.first()

    /**
     * 获取请求头所有的 Key
     */
    fun keys() = headerMap.keys

    /**
     * 获取请求头 Set<Map.Entry<String, List<String>>
     */
    fun entries() = headerMap.entries

    /**
     * 获取合并请求头 Map<String,String>
     */
    fun getRequestHeaders() = mutableMapOf<String, String>().also {
        for (entry in headerMap.entries) {
            it[entry.key] = entry.value.joinToString(";")
        }
    }

    /**
     * 获取ContentType
     */
    fun getContentType() = contentType

    /**
     * 新建 Header Builder
     */
    fun newBuilder() = Builder().apply { set(headerMap) }

    /**
     * 获取请求头个数
     */
    fun size() = headerMap.size

    class Builder {
        private val headerMap: MutableMap<String, MutableList<String>> = mutableMapOf()

        /**
         * 获取请求头参数值列表(忽略大小写)
         */
        private fun getValue(key: String): MutableList<String> {
            for (entry in headerMap.entries) {
                if (key.equals(entry.key, true)) {
                    return entry.value
                }
            }
            //如果未找到则创建一个列表返回
            return mutableListOf<String>().also { headerMap[key] = it }
        }

        /**
         * 添加请求头参数(追加，忽略大小写)
         */
        fun add(key: String, value: String) = apply {
            val list = getValue(key)
            list.add(value)
        }

        /**
         * 添加请求头参数列表(追加，忽略大小写)
         */
        fun add(key: String, value: List<String>) = apply {
            val list = getValue(key)
            list.addAll(value)
        }

        /**
         * 设置请求头参数(覆盖，忽略大小写)
         */
        fun set(key: String, value: String) = apply {
            set(key, mutableListOf(value))
        }

        /**
         * 设置请求头参数列表(覆盖，忽略大小写)
         */
        fun set(key: String, value: List<String>) = apply {
            remove(key)
            headerMap[key] = value.toMutableList()
        }

        /**
         * 设置请求头参数 Map(覆盖，忽略大小写)
         */
        fun set(headers: Map<String, List<String>>) = apply {
            for (entry in headers.entries) {
                remove(entry.key)
                headerMap[entry.key] = entry.value.toMutableList()
            }
        }

        /**
         * 移除请求头参数(忽略大小写)
         */
        fun remove(key: String) = apply {
            for (entry in headerMap.entries) {
                if (key.equals(entry.key, true)) {
                    headerMap.remove(entry.key)
                }
            }
        }

        /**
         * 复制请求头 Builder
         */
        fun copy() = Builder().also { it.headerMap.putAll(headerMap) }

        fun build(): Headers {
            var contentType = ContentType.DEFAULT

            for (entry in headerMap.entries) {
                if (CONTENT_TYPE_KEY.equals(entry.key, true)) {
                    val value = entry.value.joinToString(";")
                    contentType = ContentType.parse(value)
                    break
                }
            }

            return Headers(headerMap.toMap(), contentType)
        }
    }


}