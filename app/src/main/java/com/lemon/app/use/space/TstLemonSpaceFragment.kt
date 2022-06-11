package com.lemon.app.use.space

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.lemon.app.R
import com.lemon.app.databinding.FragmentTstBinding
import com.lemon.app.net.Net
import com.lemon.app.use.BaseFragment
import com.lemon.app.util.MLog
import java.io.PrintWriter
import java.io.StringWriter
import java.io.Writer

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

                    val writer: Writer = StringWriter()
                    val printWriter = PrintWriter(writer)
                    it.printStackTrace(printWriter)
                    var cause: Throwable? = it.cause
                    while (cause != null) {
                        cause.printStackTrace(printWriter)
                        cause = cause.cause
                    }
                    printWriter.close()
                   MLog.d(writer.toString())
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