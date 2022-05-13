package com.tube.http.util

import android.util.Log

class MLog private constructor() {

    companion object {
        private const val TAG = "TubeDemo"

        fun d(msg: String, tag: String = TAG) {
            Log.d(tag, "$msg,thread:${Thread.currentThread()}")
        }

        fun e(msg: String, throwable: Throwable? = null, tag: String = TAG) {
            Log.e(tag, msg, throwable)
        }
    }

}