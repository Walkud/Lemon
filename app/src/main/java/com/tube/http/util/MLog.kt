package com.tube.http.util

import android.util.Log

class MLog private constructor() {


    companion object {
        private const val TAG = "Tube"

        fun d(msg: String, tag: String = TAG) {
            Log.d(tag, msg)
        }

        fun e(msg: String, throwable: Throwable? = null, tag: String = TAG) {
            Log.e(tag, msg, throwable)
        }
    }

}