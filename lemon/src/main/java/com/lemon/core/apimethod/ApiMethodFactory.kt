package com.lemon.core.apimethod

import com.lemon.core.*
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

/**
 * Describe: ApiMethod 工厂类
 * 目的是首次解析目标方法，然后将结果放入缓存，随后都从缓存中获取已解析的 ApiMethod，降低系统开销
 * Created by liya.zhu on 2022/3/3
 */
internal class ApiMethodFactory(val lemon: Lemon) {

    private val methodCache: ConcurrentHashMap<Method, ApiMethod> =
        ConcurrentHashMap<Method, ApiMethod>()

    /**
     * 根据 Api 接口类与目标方法创建 ApiMethod
     * 1、优先从缓存中获取 ApiMethod 对象
     * 2、从缓存中未命中，解析 Api 接口类与目标方法生成 ApiMethod，并将对象存入缓存中
     */
    fun create(originService: Class<*>, originMethod: Method): ApiMethod {
        return methodCache[originMethod]
            ?: synchronized(methodCache) {
                methodCache[originMethod]
                    ?: getApiMethod(originService, originMethod).also {
                        methodCache[originMethod] = it
                    }
            }
    }

    /**
     * 解析 Api 接口类与目标方法生成 ApiMethod
     */
    private fun getApiMethod(originService: Class<*>, originMethod: Method) =
        ApiMethodParser(lemon, originService, originMethod).getApiMethod()
}

