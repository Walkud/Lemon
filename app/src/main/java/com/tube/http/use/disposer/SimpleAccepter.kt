package com.tube.http.use.disposer

import com.tube.http.disposer.Accepter
import com.tube.http.util.MLog

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