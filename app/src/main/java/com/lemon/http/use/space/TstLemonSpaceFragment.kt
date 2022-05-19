package com.lemon.http.use.space

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.lemon.http.R
import com.lemon.http.databinding.FragmentTstBinding
import com.lemon.http.net.Net
import com.lemon.http.use.BaseFragment
import com.lemon.http.util.MLog

/**
 * LemonSpace 使用示例语言翻译 Fragment，使用协程进行异步调度
 */
class TstLemonSpaceFragment : BaseFragment() {

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
            Net.getTstLemonSpaceApiService().languageTranslation(text)
                .bindUi(progressView, viewLifecycleOwner)
                .doStart { MLog.d("doStart call!") }
                .doEnd { MLog.d("doEnd call! $it") }
                .doError {
                    MLog.d("doError call!")
                    binding.resultTv.text = "语言翻译异常：${it.message}"
                }
                .request {
                    MLog.d("request call! $it")
                    binding.resultTv.text = Gson().toJson(it)
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}