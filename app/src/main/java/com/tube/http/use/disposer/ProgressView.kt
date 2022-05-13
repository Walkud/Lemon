package com.tube.http.use.disposer

import android.app.Activity
import android.app.ProgressDialog

/**
 * 进度 View 接口，用于显示或隐藏进度显示
 */
interface ProgressView {

    /**
     * 显示进度
     */
    fun show()

    /**
     * 隐藏进度
     */
    fun dismiss()

    /**
     * ProgressDialog 进度样式
     */
    class PvDialg(activity: Activity, msg: String = "请稍等...") : ProgressView {
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