package com.lemon.http.use.disposer

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.lemon.http.disposer.Disposer
import com.lemon.http.disposer.scheduler.Scheduler

/**
 * Disposer 统一封装扩展文件
 */

/**
 * 构建进度 UI Disposer
 */
private fun <T> buildProgressUiDisposer(
    progressView: ProgressView,
    lifecycle: Lifecycle,
    disposer: Disposer<T>,
    event: Lifecycle.Event
): Disposer<T> {
    return disposer.scheduleNet()// 网络调度
        .bindLifecycle(lifecycle, event)//绑定 UI 生命周期
        .doStart { progressView.show() } //开始时显示进度 UI
        .doEnd { progressView.dismiss() } //结束时隐藏进度 UI
}

/**
 * Activity 扩展构建 UI 进度 Diposer
 */
fun <T> ComponentActivity.createUiDisposer(
    progressView: ProgressView,
    disposer: Disposer<T>,
    event: Lifecycle.Event = Lifecycle.Event.ON_DESTROY,
): Disposer<T> {
    return buildProgressUiDisposer(progressView, lifecycle, disposer, event)
}

/**
 * Fragment 扩展构建 UI 进度 Diposer
 */
fun <T> Fragment.createUiDisposer(
    progressView: ProgressView,
    disposer: Disposer<T>,
    event: Lifecycle.Event = Lifecycle.Event.ON_DESTROY,
): Disposer<T> {
    return buildProgressUiDisposer(progressView, lifecycle, disposer, event)
}