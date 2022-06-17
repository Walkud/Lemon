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
    }

    /**
     * 根据 Key 获取请求头参数列表
     */
    fun get(key: String) = headerMap[key]

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

        private fun getValue(key: String): MutableList<String> {
            return headerMap[key] ?: mutableListOf<String>().also { headerMap[key] = it }
        }

        /**
         * 添加请求头参数(追加)
         */
        fun add(key: String, value: String) = apply {
            val list = getValue(key)
            list.add(value)
        }

        /**
         * 添加请求头参数列表(追加)
         */
        fun add(key: String, value: List<String>) = apply {
            val list = getValue(key)
            list.addAll(value)
        }

        /**
         * 设置请求头参数(覆盖)
         */
        fun set(key: String, value: String) = apply {
            headerMap[key] = mutableListOf(value)
        }

        /**
         * 设置请求头参数列表(覆盖)
         */
        fun set(key: String, value: List<String>) = apply {
            headerMap[key] = mutableListOf(*value.toTypedArray())
        }

        /**
         * 设置请求头参数 Map
         */
        fun set(headers: Map<String, List<String>>) = apply {
            for (entry in headers.entries) {
                headerMap[entry.key] = entry.value.toMutableList()
            }
        }

        /**
         * 移除请求头参数
         */
        fun remove(key: String) = apply { headerMap.remove(key) }

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