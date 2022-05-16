package com.lemon.http.use.disposer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.lemon.http.R
import com.lemon.http.bean.TstResult
import com.lemon.http.databinding.FragmentTstBinding
import com.lemon.http.disposer.Accepter
import com.lemon.http.disposer.Disposer
import com.lemon.http.disposer.scheduler.Scheduler
import com.lemon.http.net.Net
import com.lemon.http.use.BaseFragment
import com.lemon.http.util.MLog

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
                Net.getTstDisposerApiService().languageTranslation(text)
                    .doEnd {//可以自行监听结束事件
                        MLog.d("bindLifecycle Up doEnd call:$it")
                    }
                    .warp { createUiDisposer(progressView, it) }//使用统一封装的 UI  Disposer
                    .doError { binding.resultTv.text = "语言翻译异常：${it.message}" }//处理异常错误
                    .subscribe { binding.resultTv.text = Gson().toJson(it) }
            } else {
                Toast.makeText(requireContext(), "翻译的文本不能为空!", Toast.LENGTH_SHORT).show()
            }
        }
        binding.testDeaBtn.setOnClickListener {
            testEventAciton()
        }
    }

    private fun testEventAciton() {
        Disposer.create("10")
            .convert { result ->
                MLog.d("exce convert")
                Disposer.create(result.toInt() * 10)
                    .doStart { MLog.d("convert doStart call") }
                    .doEnd { MLog.d("convert doEnd call:$it") }
                    .doError { MLog.d("convert doError call :${it.message}") }
                    .convert { Disposer.create(it / 0) }
            }
            .warp { disposer ->
                MLog.d("exce warp")
                disposer.doStart { MLog.d("warp doStart call") }
                    .doEnd { MLog.d("warp doEnd call:$it") }
                    .doError { MLog.d("warp doError call :${it.message}") }
            }
            .disposerOn(Scheduler.io())
            .doStart { MLog.d("doStart call") }
            .doEnd { MLog.d("doEnd call:$it") }
            .doError { MLog.d("doError call :${it.message}") }
            .subscribe { MLog.d(it.toString()) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}