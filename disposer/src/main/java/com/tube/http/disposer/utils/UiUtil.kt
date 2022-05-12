package com.tube.http.disposer.utils

import android.os.Handler
import android.os.Looper

internal class UiUtil {

    companion object {
        /**
         * 主线程调度
         */
        fun runUiThread(runnable: Runnable) {
            if (Looper.getMainLooper() == Looper.myLooper()) {
                runnable.run()
            } else {
                val handler = Handler(Looper.getMainLooper())
                handler.post {
                    runnable.run()
                }
            }
        }
    }
}