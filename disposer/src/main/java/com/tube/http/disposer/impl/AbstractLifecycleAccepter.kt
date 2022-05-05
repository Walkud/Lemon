package com.tube.http.disposer.impl

import com.tube.http.disposer.Accepter

/**
 * Describe:抽象的生命周期事件接收器，用于统一处理生命周期事件消息传递
 * Created by liya.zhu on 2022/3/29
 */
abstract class AbstractLifecycleAccepter<T, R>(protected val accepter: Accepter<R>) : Accepter<T> {

    /**
     * 开始时间
     */
    override fun onStart() {
        accepter.onStart()
    }

    /**
     * 结束事件
     */
    override fun onEnd() {
        accepter.onEnd()
    }

    /**
     * 异常错误事件
     */
    override fun onError(throwable: Throwable) {
        accepter.onError(throwable)
    }
}