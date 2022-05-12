package com.tube.http.disposer.impl

import com.tube.http.disposer.Accepter
import com.tube.http.disposer.Disposer
import com.tube.http.disposer.scheduler.Scheduler

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