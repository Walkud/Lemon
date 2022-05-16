package com.lemon.http.disposer.impl

import com.lemon.http.disposer.Accepter
import com.lemon.http.disposer.utils.UiUtil

internal class CallAccepter<T>(private var block: ((T) -> Unit)?) : Accepter<T> {
    override fun onStart() {
    }

    override fun call(result: T) {
        UiUtil.runUiThread {
            block?.invoke(result)
        }
    }

    override fun onEnd(endState: Accepter.EndState) {
    }

    override fun onError(throwable: Throwable) {
    }
}
