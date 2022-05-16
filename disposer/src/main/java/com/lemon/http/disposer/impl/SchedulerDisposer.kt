package com.lemon.http.disposer.impl

import com.lemon.http.disposer.Accepter
import com.lemon.http.disposer.Disposer
import com.lemon.http.disposer.scheduler.Scheduler

class SchedulerDisposer<T>(private var disposer: Disposer<T>?, private var scheduler: Scheduler?) :
    Disposer<T>() {

    override fun transmit(accepter: Accepter<T>) {
        scheduler?.schedule {
            disposer?.transmit(accepter)
        }
    }

    override fun cancel() {
        disposer?.cancel()
        disposer = null
        scheduler?.cancel()
        scheduler = null
    }
}