package com.tube.http.disposer.impl

import com.tube.http.disposer.Accepter
import com.tube.http.disposer.Disposer
import com.tube.http.disposer.transformer.ConvertTransformer
import com.tube.http.onlyCall

/**
 * Describe:事件转换处理器
 * Created by liya.zhu on 2022/3/23
 */
class ConvertDisposer<T, R>(
    private val disposer: Disposer<T>,
    private val transformer: ConvertTransformer<T, R>
) : Disposer<R>() {

    override fun transmit(accepter: Accepter<R>) {
        disposer.transmit(ConvertAccepter(accepter, transformer))
    }

    /**
     * 事件转换接收器
     * 1、接收到事件 T
     * 2、执行转换，得到事件 R 处理器
     * 3、执行事件 R 处理器，继续传递事件 R
     */
    class ConvertAccepter<T, R>(
        private val accepter: Accepter<R>,
        private val transformer: ConvertTransformer<T, R>
    ) : AbstractLifecycleAccepter<T>(accepter) {

        override fun call(result: T) {
            transformer.convert(result)
                .onlyCall()
                .transmit(accepter)
        }
    }
}