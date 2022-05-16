package com.lemon.http.disposer.impl

import com.lemon.http.disposer.Accepter
import com.lemon.http.disposer.Disposer
import com.lemon.http.disposer.scheduler.Scheduler

class SchedulerDisposer<T>(
    private var disposer: Disposer<T>?,
    private var disposerScheduler: Scheduler,
    private var accepterScheduler: Scheduler
) : Disposer<T>() {

    override fun transmit(accepter: Accepter<T>) {
        disposerScheduler.schedule {
            disposer?.transmit(SchedulerAccepter(accepter, accepterScheduler))
        }
    }

    override fun cancel() {
        disposer?.cancel()
        disposer = null
        disposerScheduler.cancel()
        accepterScheduler.cancel()
    }

    /**
     * Accepter 调度执行
     */
    private class SchedulerAccepter<T>(
        accepter: Accepter<T>,
        private val scheduler: Scheduler
    ) : AbstractEventActionAccepter<T, T>(accepter) {
        override fun call(result: T) {
            scheduler.schedule {
                accepter?.call(result)
            }
        }

        override fun onStart() {
            scheduler.schedule {
                super.onStart()
            }
        }

        override fun onEnd(endState: Accepter.EndState) {
            scheduler.schedule {
                super.onEnd(endState)
            }
        }

        override fun onError(throwable: Throwable) {
            scheduler.schedule {
                super.onError(throwable)
            }
        }
    }
}