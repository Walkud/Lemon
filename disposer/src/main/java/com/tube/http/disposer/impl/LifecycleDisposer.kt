package com.tube.http.disposer.impl

import com.tube.http.disposer.Accepter
import com.tube.http.disposer.Disposer

/**
 * Describe:事件生命周期事件处理器
 * Created by liya.zhu on 2022/3/21
 */
class LifecycleDisposer<T>(
    private var disposer: Disposer<T>?,
    private var action: LifecycleAction?
) : Disposer<T>() {

    override fun transmit(accepter: Accepter<T>) {
        disposer?.transmit(LifecycleAccepter(accepter, action))
    }

    override fun onlyCall() = apply { disposer?.onlyCall() }

    /**
     * 生命周期事件接收器，用于分发 doStart、doEnd、doError 事件
     */
    class LifecycleAccepter<T>(
        private val accepter: Accepter<T>,
        private val action: LifecycleAction?
    ) : AbstractLifecycleAccepter<T>(accepter) {

        override fun call(result: T) {
            accepter.call(result)
        }

        override fun onStart() {
            super.onStart()
            //分发 doStart 事件
            if (action is LifecycleAction.StartAction) {
                action.invoke()
            }
        }

        override fun onEnd() {
            super.onEnd()
            //分发 onEnd 事件
            if (action is LifecycleAction.EndAction) {
                action.invoke()
            }
        }

        override fun onError(throwable: Throwable) {
            super.onError(throwable)
            //分发 onError
            if (action is LifecycleAction.ErrorAction) {
                action.invoke(throwable)
            }
        }
    }

    /**
     * 声明周期行为密封类
     */
    sealed class LifecycleAction {
        /**
         * start 行为
         */
        class StartAction(private val block: () -> Unit) : LifecycleAction() {
            fun invoke() {
                block.invoke()
            }
        }

        /**
         * end 行为
         */
        class EndAction(private val block: () -> Unit) : LifecycleAction() {
            fun invoke() {
                block.invoke()
            }
        }

        /**
         * 错误行为
         */
        class ErrorAction(private val block: (throwable: Throwable) -> Unit) : LifecycleAction() {
            fun invoke(throwable: Throwable) {
                block.invoke(throwable)
            }
        }
    }

    override fun cancel() {
        disposer?.cancel()
        disposer = null
        action = null
    }
}