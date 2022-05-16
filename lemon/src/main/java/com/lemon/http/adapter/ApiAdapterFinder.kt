package com.lemon.http.adapter

import com.lemon.http.adapter.ApiAdapter
import com.lemon.http.referenceName
import java.lang.reflect.Method
import java.lang.reflect.Type

/**
 * Describe: Api 返回结果适配查找器，用于根据返回类型查找对应的适配器
 * Created by liya.zhu on 2022/4/29
 */
internal class ApiAdapterFinder(private val apiAdapterFactorys: List<ApiAdapter.Factory>) {

    companion object {
        /**
         * 创建 Api 适配器查找器
         * @param apiAdapterFactorys 扩展的 Api 适配器工厂列表
         */
        fun create(apiAdapterFactorys: MutableList<ApiAdapter.Factory>): ApiAdapterFinder {
            apiAdapterFactorys.add(BuildInApiAdapterFactory())
            return ApiAdapterFinder(apiAdapterFactorys)
        }
    }

    /**
     * 查找 Api 适配器
     */
    fun findApiAdapter(type: Type, originMethod: Method): ApiAdapter {
        for (apiAdapterFactory in apiAdapterFactorys) {
            val apiAdapter = apiAdapterFactory.create(type, originMethod)

            if (apiAdapter != null) {
                return apiAdapter
            }
        }

        throw  IllegalArgumentException(
            "Could not find api method adapter " +
                    "for method:${originMethod.referenceName()},typeName:$type"
        )
    }

    /**
     * 内置的 Api 适配器
     */
    private class BuildInApiAdapterFactory : ApiAdapter.Factory() {
        override fun create(type: Type, method: Method): ApiAdapter {
            return DefaultApiAdapter
        }
    }

    /**
     * 默认的 Api 适配器
     */
    private object DefaultApiAdapter : ApiAdapter {

        override fun adapt(block: () -> Any) = block()

        override fun getActualType(type: Type) = type
    }

}