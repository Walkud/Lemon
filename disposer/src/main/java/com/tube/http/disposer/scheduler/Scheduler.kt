package com.tube.http.disposer.scheduler

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

abstract class Scheduler {

    companion object {
        fun io() = IoScheduler()
    }

    protected abstract fun run(block: () -> Unit)

    abstract fun cancel()

    fun schedule(block: () -> Unit) {
        run(block)
    }

    class IoScheduler : Scheduler() {

        private val scope = object : CoroutineScope {
            override val coroutineContext: CoroutineContext = Dispatchers.IO + Job()
        }

        override fun run(block: () -> Unit) {
            scope.launch {
                block()
            }
        }

        override fun cancel() {
            scope.cancel()
        }
    }
}
