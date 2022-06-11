package com.lemon.app.use.space

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.lemon.space.LemonSpace
import com.lemon.app.view.ProgressView

/**
 * LemonSpace 统一封装扩展文件
 */

/**
 * 构建进度 UI LemonSpace
 */
fun <T> LemonSpace<T>.bindUi(
    progressView: ProgressView,
    owner: LifecycleOwner,
    event: Lifecycle.Event = Lifecycle.Event.ON_DESTROY
): LemonSpace<T> {
    return bindLifecycle(owner, event)//绑定 UI 生命周期
        .doStart { progressView.show() } //开始时显示进度 UI
        .doEnd { progressView.dismiss() } //结束时隐藏进度 UI
}