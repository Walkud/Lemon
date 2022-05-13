package com.tube.http.disposer.impl

import com.tube.http.disposer.Accepter
import com.tube.http.disposer.Disposer

/**
 * 事件包裹处理器,用于事件处理扩展
 * 例如可以统一封装 UI 进度显示与隐藏
 */
class WarpDisposer<T, R>(
    private var disposer: Disposer<T>?,
    private var block: ((Disposer<T>) -> Disposer<R>)?
) : Disposer<R>() {

    override fun transmit(accepter: Accepter<R>) {
        disposer?.let { disposer ->
            block?.invoke(disposer)?.transmit(accepter)
        }
    }

    override fun cancel() {
        disposer?.cancel()
        disposer = null
        block = null
    }
}