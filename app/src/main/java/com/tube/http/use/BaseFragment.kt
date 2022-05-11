package com.tube.http.use

import android.app.ProgressDialog
import android.widget.Toast
import androidx.fragment.app.Fragment

open class BaseFragment : Fragment() {

    protected val progressDialog by lazy {
        ProgressDialog(requireActivity()).apply {
            setMessage("查询中...")
        }
    }

    fun showToast(msg: String) {
        requireActivity().runOnUiThread {
            Toast.makeText(requireActivity(), msg, Toast.LENGTH_SHORT).show()
        }
    }
}