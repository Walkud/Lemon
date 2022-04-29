package com.tube.http.disposer

import com.tube.http.disposer.impl.ConvertDisposer
import com.tube.http.disposer.impl.CreateDisposer
import com.tube.http.disposer.impl.LifecycleDisposer
import com.tube.http.disposer.transformer.ConvertTransformer
import com.tube.http.disposer.transformer.WarpTransformer

/**
 * Describe: 事件处理器
 * Created by liya.zhu on 2022/3/14
 */
abstract class Disposer<T> {

    companion object {
        /**
         * 创建 Disposer
         */
        fun <T> create(value: T) = CreateDisposer(value)
    }

    /**
     * 事件接收器传递
     */
    internal abstract fun transmit(accepter: Accepter<T>)

    /**
     * 仅传递 Call 事件，在 Convert 场景会被调用
     */
    internal abstract fun onlyCall(): Disposer<T>

    /**
     * 取消事件传递
     */
    abstract fun cancel()

    /**
     * 事件包裹，用于事件处理扩展
     */
    fun <R> warp(transformer: WarpTransformer<T, R>): Disposer<R> = transformer.transform(this)

    /**
     * 事件转换，用于将事件 T 转换为事件 R 场景
     * 例如：有 A,B 两个接口，B 接口依赖 A 接口的返回数据，则可以使用该方法
     */
    fun <R> convert(transformer: ConvertTransformer<T, R>) = ConvertDisposer(this, transformer)

    /**
     * 开始事件监听，可以用于进度弹框、开始与结束按钮状态转换场景，与 doEnd 事件结合使用
     */
    fun doStart(block: () -> Unit): Disposer<T> =
        LifecycleDisposer(this, LifecycleDisposer.LifecycleAction.StartAction(block))

    /**
     * 错误事件监听
     */
    fun doError(block: (throwable: Throwable) -> Unit): Disposer<T> =
        LifecycleDisposer(this, LifecycleDisposer.LifecycleAction.ErrorAction(block))

    /**
     * 完成事件监听，可以用于进度弹框、开始与结束按钮状态转换场景，与 doStart 事件结合使用
     */
    fun doEnd(block: () -> Unit): Disposer<T> =
        LifecycleDisposer(this, LifecycleDisposer.LifecycleAction.EndAction(block))

    /**
     * 订阅，开始传递事件
     */
    fun subscribe(accepter: Accepter<T>) = apply { transmit(accepter) }
}