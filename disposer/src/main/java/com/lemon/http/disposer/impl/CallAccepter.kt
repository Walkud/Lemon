package com.lemon.http.disposer.impl

import com.lemon.http.disposer.Accepter
import com.lemon.http.disposer.utils.UiUtil

/**
 * 仅接收 Call 事件接收器
 */
internal class CallAccepter<T>(private var block: ((T) -> Unit)) : Accepter<T> {
    override fun onStart() {
    }

    override fun call(result: T) {
        block.invoke(result)
    }

    override fun onEnd(endState: Accepter.EndState) {
    }

    override fun onError(throwable: Throwable) {
    }
}
