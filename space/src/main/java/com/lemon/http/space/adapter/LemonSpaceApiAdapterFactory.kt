package com.lemon.http.space.adapter

import com.lemon.http.space.LemonSpace
import com.lemon.http.adapter.ApiAdapter
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * LemonSpace Api 返回结果转换器工厂
 * Created by liya.zhu on 2022/4/29
 */
class LemonSpaceApiAdapterFactory : ApiAdapter.Factory() {

    override fun create(type: Type, method: Method): ApiAdapter? {
        val rawType = getRawType(type)
        if (LemonSpace::class.java.isAssignableFrom(rawType)) {
            if (type is ParameterizedType) {
                return LemonSpaceApiAdapter
            } else {
                throw IllegalArgumentException(
                    "LemonScope generic types must be defined!" +
                            "for returnTypeName:$type"
                )
            }
        }
        return null
    }

    /**
     * LemonScope Api 返回结果转换器
     */
    private object LemonSpaceApiAdapter : ApiAdapter {
        override fun adapt(block: () -> Any): Any {
            return LemonSpace.create(block)
        }

        override fun getActualType(type: Type): Type {
            if (type is ParameterizedType) {
                return type.actualTypeArguments[0]
            }

            throw IllegalArgumentException(
                "LemonScope generic types must be defined!" +
                        "for ,returnTypeName:$type"
            )
        }
    }
}