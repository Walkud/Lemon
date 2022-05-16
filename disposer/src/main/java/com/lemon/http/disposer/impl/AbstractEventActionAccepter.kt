package com.lemon.http.disposer.impl

import com.lemon.http.disposer.Accepter

/**
 * Describe:抽象的事件行为接收器，用于统一处理事件行为消息传递
 * Created by liya.zhu on 2022/3/29
 */
abstract class AbstractEventActionAccepter<T, R>(protected var accepter: Accepter<R>?) :
    Accepter<T> {

    /**
     * 开始时间
     */
    override fun onStart() {
        accepter?.onStart()
    }

    /**
     * 结束事件
     */
    override fun onEnd(endState: Accepter.EndState) {
        accepter?.onEnd(endState)
    }

    /**
     * 异常错误事件
     */
    override fun onError(throwable: Throwable) {
        accepter?.onError(throwable)
    }
}