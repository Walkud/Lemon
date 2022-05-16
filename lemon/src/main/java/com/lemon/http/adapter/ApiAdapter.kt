package com.lemon.http.adapter

import com.lemon.http.asRawType
import java.lang.reflect.Method
import java.lang.reflect.Type

/**
 * Api 返回结果转换器
 * Created by liya.zhu on 2022/4/29
 */
interface ApiAdapter {

    /**
     * 适配
     */
    fun adapt(block: () -> Any): Any

    /**
     * 返回结果实际需要转换的类型
     */
    fun getActualType(type: Type): Type

    /**
     * Api 返回结果转换器工厂
     */
    abstract class Factory {

        /**
         * 创建 Api 返回结果转换器
         */
        abstract fun create(type: Type, method: Method): ApiAdapter?

        /**
         * 返回此类型的类或接口的类型，例如:List<String> 返回 List
         */
        fun getRawType(type: Type) = type.asRawType()
    }
}