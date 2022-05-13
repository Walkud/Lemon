package com.tube.http.disposer.scheduler

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * 调度器
 */
abstract class Scheduler {

    companion object {
        /**
         * 获取主线程调度器
         */
        fun main() = CoroutineScheduler(Dispatchers.Main)

        /**
         * 获取 IO 调度器
         */
        fun io() = CoroutineScheduler(Dispatchers.IO)
    }

    /**
     * 执行
     */
    protected abstract fun run(block: () -> Unit)

    /**
     * 取消调度执行
     */
    abstract fun cancel()

    /**
     * 安排调度
     */
    fun schedule(block: () -> Unit) {
        run(block)
    }

    /**
     * 协程调度器
     */
    class CoroutineScheduler(coroutineDispatcher: CoroutineDispatcher) : Scheduler() {
        /**
         *
         */
        private val scope = object : CoroutineScope {
            override val coroutineContext: CoroutineContext = coroutineDispatcher + Job()
        }

        /**
         * 通过指定协程作用域进行调度执行
         */
        override fun run(block: () -> Unit) {
            scope.launch {
                block()
            }
        }

        /**
         * 取消协程
         */
        override fun cancel() {
            scope.cancel()
        }
    }
}
