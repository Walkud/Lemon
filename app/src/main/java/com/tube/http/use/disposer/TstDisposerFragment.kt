package com.tube.http.use.disposer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.tube.http.R
import com.tube.http.bean.TstResult
import com.tube.http.databinding.FragmentTstBinding
import com.tube.http.disposer.Disposer
import com.tube.http.net.Net
import com.tube.http.use.BaseFragment
import kotlinx.coroutines.*
import kotlin.concurrent.thread

/**
 * 简单使用示例语言翻译 Fragment
 */
class TstDisposerFragment : BaseFragment() {

    private var _binding: FragmentTstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentTstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        binding.queryBtn.setOnClickListener {
            val text = binding.textEt.text.toString()
            if (text.isNotEmpty()) {
                thread {
                    Net.getTstDisposerApiService().languageTranslation(text)
                        .bindLifecycle(lifecycle, Lifecycle.Event.ON_DESTROY)
                        .subscribe(object : SimpleAccepter<TstResult>() {
                            override fun onStart() {
                                super.onStart()
                                requireActivity().runOnUiThread {
                                    progressDialog.show()
                                }
                                Thread.sleep(5000)
                            }

                            override fun call(result: TstResult) {
                                super.call(result)
                                requireActivity().runOnUiThread {
                                    binding.resultTv.text = Gson().toJson(result)
                                }
                            }

                            override fun onError(throwable: Throwable) {
                                super.onError(throwable)
                                requireActivity().runOnUiThread {
                                    binding.resultTv.text = "语言翻译异常：${throwable.message}"
                                }
                            }

                            override fun onEnd() {
                                super.onEnd()
                                requireActivity().runOnUiThread {
                                    progressDialog.dismiss()
                                }
                            }
                        })
                }
//                GlobalScope.launch(Dispatchers.IO) {
//                    delay(3000)
//                    val a = 100 / 0

//                }
            } else {
                Toast.makeText(requireContext(), "翻译的文本不能为空!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}