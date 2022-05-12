package com.tube.http.disposer

/**
 * Describe: 事件接收器
 * Created by liya.zhu on 2022/3/14
 */
interface Accepter<T> {

    /**
     * 开始事件，事件开始传递前触发
     */
    fun onStart()

    /**
     * 事件传递
     */
    fun call(result: T)

    /**
     * 结束事件，事件传递完成后触发
     * 注意：当事件传递过程中出现异常后，该方法也会被调用
     */
    fun onEnd(endState: EndState)

    /**
     * 异常错误事件
     */
    fun onError(throwable: Throwable)

    /**
     * 结束状态
     */
    enum class EndState {
        Normal,//正常结束
        Cancel;//取消结束
    }
}