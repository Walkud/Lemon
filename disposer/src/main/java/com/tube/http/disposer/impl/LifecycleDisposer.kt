package com.tube.http.disposer.impl

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.tube.http.disposer.Accepter
import com.tube.http.disposer.Disposer

class LifecycleDisposer<T>(
    private var disposer: Disposer<T>?,
    lifecycle: Lifecycle,
    bindEvent: Lifecycle.Event
) : Disposer<T>() {

    private val lifecycleObserver = UiLifecycleObserver(this, lifecycle, bindEvent)
    private var accepter: Accepter<T>? = null

    init {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            lifecycle.addObserver(lifecycleObserver)
        } else {
            val handler = Handler(Looper.getMainLooper())
            handler.post {
                disposer?.let {
                    lifecycle.addObserver(lifecycleObserver)
                }
            }
        }
    }

    override fun transmit(accepter: Accepter<T>) {
        this.accepter = accepter
        disposer?.transmit(accepter)
    }

    override fun cancel() {
        disposer?.cancel()
        disposer = null
        accepter?.onCancel()
        accepter = null
    }

    private class UiLifecycleObserver<T>(
        private var lifecycleDisposer: LifecycleDisposer<T>?,
        private var lifecycle: Lifecycle?,
        private val bindEvent: Lifecycle.Event
    ) : LifecycleObserver {

        @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
        fun eventOnChange(owner: LifecycleOwner, event: Lifecycle.Event) {
            Log.d(
                "LifecycleDisposer",
                "eventOnChange:$event,bindEvent:$bindEvent,isMainThread:${Looper.getMainLooper() == Looper.myLooper()}"
            )
            if (event == bindEvent) {
                Log.d("LifecycleDisposer", "lifecycleDisposer call cancel")
                lifecycleDisposer?.cancel()
                lifecycleDisposer = null
                lifecycle?.removeObserver(this)
                lifecycle = null
            }
        }
    }
}