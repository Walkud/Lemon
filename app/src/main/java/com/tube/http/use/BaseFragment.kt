package com.tube.http.use

import android.widget.Toast
import androidx.fragment.app.Fragment
import com.tube.http.use.disposer.ProgressView

open class BaseFragment : Fragment() {

    protected val progressView by lazy {
        ProgressView.PvDialg(requireActivity())
    }

    fun showToast(msg: String) {
        requireActivity().runOnUiThread {
            Toast.makeText(requireActivity(), msg, Toast.LENGTH_SHORT).show()
        }
    }
}