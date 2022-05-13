package com.tube.http.use.disposer

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.tube.http.disposer.Disposer
import com.tube.http.disposer.scheduler.Scheduler

private fun <T> buildProgressUiDisposer(
    progressView: ProgressView,
    lifecycle: Lifecycle,
    disposer: Disposer<T>,
    event: Lifecycle.Event
): Disposer<T> {
    return disposer.disposerOn(Scheduler.io())
        .bindLifecycle(lifecycle, event)
        .doStart { progressView.show() }
        .doEnd { progressView.dismiss() }
}

fun <T> ComponentActivity.createUiDisposer(
    progressView: ProgressView,
    disposer: Disposer<T>,
    event: Lifecycle.Event = Lifecycle.Event.ON_DESTROY,
): Disposer<T> {
    return buildProgressUiDisposer(progressView, lifecycle, disposer, event)
}

fun <T> Fragment.createUiDisposer(
    progressView: ProgressView,
    disposer: Disposer<T>,
    event: Lifecycle.Event = Lifecycle.Event.ON_DESTROY,
): Disposer<T> {
    return buildProgressUiDisposer(progressView, lifecycle, disposer, event)
}