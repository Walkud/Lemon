package com.tube.http.disposer

import androidx.lifecycle.Lifecycle
import com.tube.http.disposer.impl.*
import com.tube.http.disposer.scheduler.Scheduler
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
     * 绑定 UI 生命周期
     */
    fun bindLifecycle(lifecycle: Lifecycle, bindEvent: Lifecycle.Event) =
        LifecycleDisposer(this, lifecycle, bindEvent)

    /**
     * 处理器执行调度
     */
    fun disposerOn(scheduler: Scheduler) = SchedulerDisposer(this, scheduler)

    /**
     * 开始事件监听，可以用于进度弹框、开始与结束按钮状态转换场景，与 doEnd 事件结合使用，UI 线程中回调
     */
    fun doStart(block: () -> Unit): Disposer<T> =
        EventActionDisposer(this, EventActionDisposer.EventAction.StartAction(block))

    /**
     * 错误事件监听，UI 线程中回调
     */
    fun doError(block: (throwable: Throwable) -> Unit): Disposer<T> =
        EventActionDisposer(this, EventActionDisposer.EventAction.ErrorAction(block))

    /**
     * 完成事件监听，可以用于进度弹框、开始与结束按钮状态转换场景，与 doStart 事件结合使用，UI 线程中回调
     */
    fun doEnd(block: (endState: Accepter.EndState) -> Unit): Disposer<T> =
        EventActionDisposer(this, EventActionDisposer.EventAction.EndAction(block))

    /**
     * 订阅，开始传递事件
     */
    fun subscribe(accepter: Accepter<T>) = transmit(SubscribeAccepter(accepter))
}