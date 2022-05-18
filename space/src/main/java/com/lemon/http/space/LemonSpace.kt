package com.lemon.http.space

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Lemon 空间是普通网络请求场景的封装类，可以用于 UI 生命周期绑定、UI 进度切换、自动切换 UI 与 IO 线程。
 * @param apiBlock api 高阶函数，调用可发起实际请求
 */
class LemonSpace<T> private constructor(
    private val apiBlock: () -> T,
) {

    companion object {
        fun <T> create(apiBlock: () -> T) = LemonSpace(apiBlock)
    }

    private val lemonSpace = LemonScope()
    private var cancelled = false //是否已经取消，即 UI 页面是否已经销毁
    private var doStartBlocks: MutableList<() -> Unit> = mutableListOf() //开始回调函数集合
    private var doErrorBlocks: MutableList<(e: Exception) -> Unit> = mutableListOf()//异常回调函数集合
    private var doEndBlocks: MutableList<(endState: EndState) -> Unit> = mutableListOf()//完成回调函数集合
    private var requestBlock: ((T) -> Unit)? = null //请求回调函数
    private var lifecycle: Lifecycle? = null
    private var uiLifecycleObserver: UiLifecycleObserver? = null

    /**
     * 开始事件监听，可以用于进度弹框、开始与结束按钮状态转换场景，常与 doEnd 事件结合使用
     */
    fun doStart(block: () -> Unit) = apply { doStartBlocks.add(block) }

    /**
     * 错误事件监听
     */
    fun doError(block: (e: Exception) -> Unit) = apply { doErrorBlocks.add(block) }

    /**
     * 完成事件监听，可以用于进度弹框、开始与结束按钮状态转换场景，常与 doStart 事件结合使用
     */
    fun doEnd(block: (endState: EndState) -> Unit) = apply { doEndBlocks.add(block) }

    /**
     * 绑定 UI 生命周期
     */
    fun bindLifecycle(lifecycle: Lifecycle, bindEvent: Lifecycle.Event) = apply {
        lemonSpace.launch {
            //主线程中执行添加
            addUiLifecycle(lifecycle, bindEvent)
        }
    }

    /**
     * 添加 UI 生命周期监听
     */
    private fun addUiLifecycle(lifecycle: Lifecycle, bindEvent: Lifecycle.Event) {
        if (!cancelled) {
            this.lifecycle = lifecycle
            this.uiLifecycleObserver = UiLifecycleObserver(bindEvent).also {
                lifecycle.addObserver(it)
            }
        }
    }

    /**
     * 发起请求
     */
    fun request(block: (T) -> Unit) {
        requestBlock = block
        if (cancelled) {
            return
        }
        lemonSpace.launch {
            try {
                //触发开始事件
                doStartBlocks.forEach { it.invoke() }
                //异步执行网络请求
                val result = withContext(Dispatchers.IO) {
                    apiBlock()
                }
                //回调结果
                requestBlock?.invoke(result)
            } catch (e: Exception) {
                //触发异常事件
                doErrorBlocks.forEach { it.invoke(e) }
            } finally {
                //触发完成事件
                doEndBlocks.forEach { it.invoke(EndState.Normal) }
                clear()
            }
        }
    }

    /**
     * 取消
     */
    private fun cancel() {
        cancelled = true
        lemonSpace.cancel()
        doEndBlocks.forEach { it.invoke(EndState.Cancel) }
        clear()
    }

    /**
     * 清除引用
     */
    private fun clear() {
        doStartBlocks.clear()
        doErrorBlocks.clear()
        doEndBlocks.clear()
        requestBlock = null
        uiLifecycleObserver?.let {
            lifecycle?.removeObserver(it)
            uiLifecycleObserver = null
            lifecycle = null
        }
    }

    /**
     * LemoSpace 协程作用域（主线程）
     */
    private inner class LemonScope : CoroutineScope {
        override val coroutineContext: CoroutineContext
            get() = Dispatchers.Main.immediate + SupervisorJob()
    }

    /**
     * UI 生命周期观察者
     */
    private inner class UiLifecycleObserver(
        private val bindEvent: Lifecycle.Event
    ) : LifecycleObserver {

        @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
        fun eventOnChange(owner: LifecycleOwner, event: Lifecycle.Event) {
            if (event == bindEvent || event == Lifecycle.Event.ON_DESTROY) {
                //绑定的生命周期触发或页面销毁，清除引用
                cancel()
            }
        }
    }

    /**
     * 结束状态
     */
    enum class EndState {
        Normal,//正常结束
        Cancel;//取消结束
    }

}