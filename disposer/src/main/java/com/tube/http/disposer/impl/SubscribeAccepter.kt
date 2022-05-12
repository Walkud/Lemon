package com.tube.http.disposer.impl

import com.tube.http.disposer.Accepter
import com.tube.http.disposer.utils.UiUtil

internal class SubscribeAccepter<T>(private var accepter: Accepter<T>?) : Accepter<T> {
    override fun onStart() {
        UiUtil.runUiThread {
            accepter?.onStart()
        }
    }

    override fun call(result: T) {
        UiUtil.runUiThread {
            accepter?.call(result)
        }
    }

    override fun onEnd(endState: Accepter.EndState) {
        UiUtil.runUiThread {
            accepter?.onEnd(endState)
            accepter = null
        }
    }

    override fun onError(throwable: Throwable) {
        UiUtil.runUiThread {
            accepter?.onError(throwable)
        }
    }
}
