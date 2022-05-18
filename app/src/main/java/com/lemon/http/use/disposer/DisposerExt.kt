package com.lemon.http.use.disposer

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.lemon.http.disposer.Disposer
import com.lemon.http.view.ProgressView

/**
 * Disposer 统一封装扩展文件
 */

/**
 * 构建进度 UI Disposer
 */
fun <T> Disposer<T>.bindUi(
    progressView: ProgressView,
    lifecycle: Lifecycle,
    event: Lifecycle.Event = Lifecycle.Event.ON_DESTROY
): Disposer<T> {
    return scheduleNet()// 网络调度
        .bindLifecycle(lifecycle, event)//绑定 UI 生命周期
        .doStart { progressView.show() } //开始时显示进度 UI
        .doEnd { progressView.dismiss() } //结束时隐藏进度 UI
}