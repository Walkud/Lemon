package com.tube.http

import com.tube.http.disposer.Accepter

/**
 * Describe:
 * Created by liya.zhu on 2022/3/29
 */
abstract class SampleAccepter<T> : Accepter<T> {
    override fun onStart() {
    }

    override fun onEnd(endState: Accepter.EndState) {
    }

    override fun onError(throwable: Throwable) {
        throwable.printStackTrace()
    }
}