package com.tube.http.disposer.transformer

import com.tube.http.disposer.Disposer

/**
 * Describe:事件包裹转换器，将事件 Disposer<T> 转换为 Disposer<R>
 * Created by liya.zhu on 2022/3/21
 */
interface WarpTransformer<T, R> {

    fun transform(disposer: Disposer<T>): Disposer<R>
}