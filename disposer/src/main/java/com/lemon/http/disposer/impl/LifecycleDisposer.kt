package com.lemon.http.disposer.impl

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.lemon.http.disposer.Accepter
import com.lemon.http.disposer.Disposer
import com.lemon.http.disposer.utils.UiUtil

/**
 * UI 生命周期处理器
 */
class LifecycleDisposer<T>(
    private var disposer: Disposer<T>?,
    private var lifecycle: Lifecycle?,
    bindEvent: Lifecycle.Event
) : Disposer<T>() {

    private val lifecycleObserver = UiLifecycleObserver(this, bindEvent)
    private var lifecycleAccepter: LifecycleAccepter<T>? = null

    init {
        UiUtil.runUiThread {
            //添加 UI 生命周期监听
            lifecycle?.addObserver(lifecycleObserver)
        }
    }


    override fun transmit(accepter: Accepter<T>) {
        disposer?.let {
            this.lifecycleAccepter = LifecycleAccepter(accepter)
            lifecycleAccepter?.let { accepter ->
                it.transmit(accepter)
            }
        }
    }

    /**
     * 取消操作
     * 1、移除 UI 生命周期监听
     * 2、调用接收器结束事件(取消状态)
     * 3、传递取消事件
     */
    override fun cancel() {
        removeObserver()
        lifecycleAccepter?.onEnd(Accepter.EndState.Cancel)
        lifecycleAccepter = null
        disposer?.cancel()
        disposer = null
    }

    /**
     * 移除 UI 生命周期监听
     */
    private fun removeObserver() {
        UiUtil.runUiThread {
            lifecycle?.removeObserver(lifecycleObserver)
            lifecycle = null
        }
    }

    /**
     * 事件传递结束事件接收器，用于移除 UI 生命周期监听
     */
    private inner class LifecycleAccepter<T>(accepter: Accepter<T>) :
        AbstractEventActionAccepter<T, T>(accepter) {
        override fun call(result: T) {
            accepter?.call(result)
        }

        override fun onEnd(endState: Accepter.EndState) {
            super.onEnd(endState)
            if (endState == Accepter.EndState.Normal) {
                removeObserver()
            }
            accepter = null
        }
    }

    /**
     * UI 生命周期观察者
     */
    private class UiLifecycleObserver<T>(
        private var lifecycleDisposer: LifecycleDisposer<T>?,
        private val bindEvent: Lifecycle.Event
    ) : LifecycleObserver {

        @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
        fun eventOnChange(owner: LifecycleOwner, event: Lifecycle.Event) {
            if (event == bindEvent) {
                lifecycleDisposer?.cancel()
                lifecycleDisposer = null
            }
        }
    }
}