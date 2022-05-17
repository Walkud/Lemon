package com.lemon.http.disposer.scheduler

import com.lemon.http.disposer.utils.UiUtil
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * 调度器
 */
abstract class Scheduler {

    companion object {

        /**
         * unconfined 调度器
         */
        fun unconfined() = CoroutineScheduler(Dispatchers.Unconfined + SupervisorJob())

        /**
         * 默认调度器
         */
        fun default() = CoroutineScheduler(Dispatchers.Default + SupervisorJob())

        /**
         * 获取主线程调度器
         */
        fun main() = CoroutineScheduler(Dispatchers.Main.immediate + SupervisorJob())

        /**
         * 获取 IO 调度器
         */
        fun io() = CoroutineScheduler(Dispatchers.IO + SupervisorJob())

        /**
         * 自定义作用域调度器
         */
        fun custom(context: CoroutineContext) = CoroutineScheduler(context)
    }

    /**
     * 通过指定调度器调度执行
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
    class CoroutineScheduler(context: CoroutineContext) : Scheduler() {
        /**
         * 协程作用域
         */
        private val scope = object : CoroutineScope {
            override val coroutineContext: CoroutineContext = context
        }

        /**
         * 是否取消调度
         */
        private var isCancel = false

        /**
         * 通过指定协程作用域进行调度执行
         */
        override fun run(block: () -> Unit) {
            if (!isCancel) {
                scope.launch {
                    block()
                }
            }
        }

        /**
         * 取消协程
         */
        override fun cancel() {
            isCancel = true
            scope.cancel()
        }
    }
}
