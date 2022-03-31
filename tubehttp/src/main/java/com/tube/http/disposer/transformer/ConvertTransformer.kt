package com.tube.http.disposer.transformer

import com.tube.http.disposer.Disposer

/**
 * Describe:事件转换器，将事件 T 转换为 Disposer<R>
 * Created by liya.zhu on 2022/3/25
 */
interface ConvertTransformer<T, R> {

    fun convert(result: T): Disposer<R>
}