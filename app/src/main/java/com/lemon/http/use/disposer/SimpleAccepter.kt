package com.lemon.http.use.disposer

import com.lemon.http.disposer.Accepter
import com.lemon.http.util.MLog

/**
 * 可以根据实际场景实现 Accepter 的配置方法，可以避免每个订阅都需要所有方法代码臃肿
 */
open class SimpleAccepter<T> : Accepter<T> {

    override fun onStart() {
        MLog.d("onStart")
    }

    override fun call(result: T) {
        MLog.d("call:$result")
    }

    override fun onEnd(endState: Accepter.EndState) {
        MLog.d("onEnd:$endState")
    }

    override fun onError(throwable: Throwable) {
        MLog.d("onError:${throwable.message}")
    }
}