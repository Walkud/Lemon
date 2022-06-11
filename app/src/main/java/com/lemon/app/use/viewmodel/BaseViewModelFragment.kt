package com.lemon.app.use.viewmodel

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.lemon.app.use.BaseFragment
import com.lemon.app.use.viewmodel.model.BaseViewModel

abstract class BaseViewModelFragment<VM : BaseViewModel> : BaseFragment() {

    protected val viewModel by lazy {
        ViewModelProvider(
            viewModelStore,
            defaultViewModelProviderFactory
        ).get(getViewModelClass())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //页面销毁自动移除
        viewModel.showProgress.observe(viewLifecycleOwner) { isShow ->
            if (isShow) {
                progressView.show()
            } else {
                progressView.dismiss()
            }
        }

        //页面销毁自动移除
        viewModel.showToast.observe(viewLifecycleOwner) {
            showToast(it)
        }
    }

    abstract fun getViewModelClass(): Class<VM>
}