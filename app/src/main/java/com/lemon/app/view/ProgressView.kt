package com.lemon.app.view

import android.app.Activity
import android.app.ProgressDialog

/**
 * 进度 View 接口，用于显示或隐藏进度显示
 */
abstract class ProgressView {

    /**
     * 显示进度
     */
    open fun show() {}

    /**
     * 正常回调进度
     */
    open fun call() {}

    /**
     * 错误回调进度
     */
    open fun error(e: Exception) {}

    /**
     * 隐藏进度
     */
    open fun dismiss() {}

    /**
     * ProgressDialog 进度样式
     */
    class PvDialg(activity: Activity, msg: String = "请稍等...") : ProgressView() {
        private val progressDialog by lazy {
            ProgressDialog(activity).apply {
                setMessage(msg)
            }
        }

        override fun show() {
            progressDialog.show()
        }

        override fun dismiss() {
            if (progressDialog.isShowing) {
                progressDialog.dismiss()
            }
        }
    }
}