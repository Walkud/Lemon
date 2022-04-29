package com.tube.http.disposer.adapter

import com.tube.http.adapter.ApiAdapter
import com.tube.http.disposer.Disposer
import com.tube.http.disposer.transformer.ConvertTransformer
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Disposer Api 返回结果转换器工厂
 * Created by liya.zhu on 2022/4/29
 */
class DisposerApiAdapterFactory : ApiAdapter.Factory() {

    override fun create(type: Type, method: Method): ApiAdapter? {
        val rawType = getRawType(type)
        if (Disposer::class.java.isAssignableFrom(rawType)) {
            if (type is ParameterizedType) {
                return DisposerApiAdapter
            } else {
                throw IllegalArgumentException(
                    "Disposer generic types must be defined!" +
                            "for returnTypeName:$type"
                )
            }
        }
        return null
    }

    /**
     * Disposer Api 返回结果转换器
     */
    private object DisposerApiAdapter : ApiAdapter {
        override fun adapt(block: () -> Any): Any {
            return Disposer.create(block)
                .convert(object : ConvertTransformer<() -> Any, Any> {
                    override fun convert(result: () -> Any): Disposer<Any> {
                        return Disposer.create(result())
                    }
                })
        }

        override fun getActualType(type: Type): Type {
            if (type is ParameterizedType) {
                return type.actualTypeArguments[0]
            }

            throw IllegalArgumentException(
                "Disposer generic types must be defined!" +
                        "for ,returnTypeName:$type"
            )
        }
    }
}