package com.tube.http.disposer.impl

import com.tube.http.disposer.Accepter
import com.tube.http.disposer.Disposer
import com.tube.http.disposer.utils.UiUtil

/**
 * Describe:事件行为处理器
 * Created by liya.zhu on 2022/3/21
 */
class EventActionDisposer<T>(
    private var disposer: Disposer<T>?,
    private var action: EventAction?
) : Disposer<T>() {

    override fun transmit(accepter: Accepter<T>) {
        disposer?.transmit(EventActionAccepter(accepter, action))
    }

    /**
     * 事件行为事件接收器，用于分发 doStart、doEnd、doError 事件
     */
    private class EventActionAccepter<T>(
        accepter: Accepter<T>,
        private val action: EventAction?
    ) : AbstractEventActionAccepter<T, T>(accepter) {

        override fun call(result: T) {
            accepter.call(result)
        }

        override fun onStart() {
            super.onStart()
            //分发 doStart 事件
            if (action is EventAction.StartAction) {
                action.invoke()
            }
        }

        override fun onEnd(endState: Accepter.EndState) {
            super.onEnd(endState)
            //分发 onEnd 事件
            if (action is EventAction.EndAction) {
                action.invoke(endState)
            }
        }

        override fun onError(throwable: Throwable) {
            super.onError(throwable)
            //分发 onError
            if (action is EventAction.ErrorAction) {
                action.invoke(throwable)
            }
        }
    }

    /**
     * 事件行为密封类
     */
    sealed class EventAction {
        /**
         * start 行为，UI 线程中回调
         */
        class StartAction(private val block: () -> Unit) : EventAction() {
            fun invoke() {
                UiUtil.runUiThread {
                    block.invoke()
                }
            }
        }

        /**
         * end 行为，UI 线程中回调
         */
        class EndAction(private val block: (endState: Accepter.EndState) -> Unit) : EventAction() {
            fun invoke(endState: Accepter.EndState) {
                UiUtil.runUiThread {
                    block.invoke(endState)
                }
            }
        }

        /**
         * 错误行为，UI 线程中回调
         */
        class ErrorAction(private val block: (throwable: Throwable) -> Unit) : EventAction() {
            fun invoke(throwable: Throwable) {
                UiUtil.runUiThread {
                    block.invoke(throwable)
                }
            }
        }
    }

    override fun cancel() {
        disposer?.cancel()
        disposer = null
        //分发 onEnd 事件
        (action as? EventAction.EndAction)?.invoke(Accepter.EndState.Cancel)
        action = null
    }
}