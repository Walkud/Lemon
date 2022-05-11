package com.tube.http.use.disposer

import android.util.Log
import com.tube.http.disposer.Accepter

open class SimpleAccepter<T> : Accepter<T> {

    companion object {
        private val TAG = SimpleAccepter::class.java.simpleName
    }

    override fun onStart() {
        Log.d(TAG, "onStart")
    }

    override fun call(result: T) {
        Log.d(TAG, "call:$result")
    }

    override fun onEnd() {
        Log.d(TAG, "onEnd")
    }

    override fun onError(throwable: Throwable) {
        Log.d(TAG, "onError:${throwable.message}")
    }

    override fun onCancel() {
        Log.d(TAG, "onCancel")
    }
}