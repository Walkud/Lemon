package com.lemon.app.use.simple

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.lemon.app.R
import com.lemon.app.databinding.FragmentTstBinding
import com.lemon.app.net.Net
import com.lemon.app.use.BaseFragment
import kotlinx.coroutines.*

/**
 * 简单使用示例语言翻译 Fragment，使用协程进行异步调度
 */
class TstFragment : BaseFragment(), CoroutineScope by MainScope() {

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
            launch {
                if (text.isNotEmpty()) {
                    progressView.show()
                    binding.resultTv.text = try {
                        val result = withContext(Dispatchers.IO) {
                            Net.getTstApiService().languageTranslation(text)
                        }
                        Gson().toJson(result)
                    } catch (e: Exception) {
                        "语言翻译异常：${e.message}"
                    }
                    progressView.dismiss()
                } else {
                    Toast.makeText(requireContext(), "翻译的文本不能为空!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cancel()
    }
}