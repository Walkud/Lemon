package com.tube.http.disposer.impl

import com.tube.http.disposer.Accepter
import com.tube.http.disposer.Disposer

/**
 * Describe:Create 处理器，事件传递源头，支持生命周期传递
 * 直接通过 Disposer.create(value) 或者 CreateDisposer(value) 创建实例
 * Created by liya.zhu on 2022/3/23
 */
class CreateDisposer<T>(private val value: T) : Disposer<T>() {

    /**
     * 是否仅传递 call 事件，不传递生命周期事件
     */
    private var onlyCall = false

    /**
     * 是否取消
     */
    private var cancel = false

    /**
     * 设置仅传递 call 事件 (仅内部使用)
     * 当该实例为非事件源头时需调用此方法，如未调用则可能出现多次触发传递生命周期事件
     */
    override fun onlyCall() = apply { onlyCall = true }

    override fun transmit(accepter: Accepter<T>) {
        if (cancel) {
            //已取消，不传递事件
            return
        }

        if (onlyCall) {
            accepter.call(value)
        } else {
            try {
                accepter.onStart()
                accepter.call(value)
            } catch (t: Throwable) {
                accepter.onError(t)
            } finally {
                accepter.onEnd()
            }
        }
    }

    override fun cancel() {
        cancel = true
    }
}