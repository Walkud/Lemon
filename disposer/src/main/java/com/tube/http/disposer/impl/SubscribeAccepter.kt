package com.tube.http.disposer.impl

import com.tube.http.disposer.Accepter

internal class SubscribeAccepter<T>(private var accepter: Accepter<T>?) : Accepter<T> {
    override fun onStart() {
        accepter?.onStart()
    }

    override fun call(result: T) {
        accepter?.call(result)
    }

    override fun onEnd(endState: Accepter.EndState) {
        accepter?.onEnd(endState)
        accepter = null
    }

    override fun onError(throwable: Throwable) {
        accepter?.onError(throwable)
    }
}
