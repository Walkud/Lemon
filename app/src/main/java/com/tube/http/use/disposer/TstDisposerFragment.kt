package com.tube.http.use.disposer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.tube.http.R
import com.tube.http.bean.TstResult
import com.tube.http.databinding.FragmentTstBinding
import com.tube.http.disposer.Accepter
import com.tube.http.disposer.Disposer
import com.tube.http.disposer.scheduler.Scheduler
import com.tube.http.net.Net
import com.tube.http.use.BaseFragment
import com.tube.http.util.MLog

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
                    .doEnd {
                        MLog.d("bindLifecycle Up doEnd call:$it")
                    }
                    .warp { createUiDisposer(progressView, it) }
                    .subscribe(object : SimpleAccepter<TstResult>() {
                        override fun call(result: TstResult) {
                            super.call(result)
                            binding.resultTv.text = Gson().toJson(result)
                        }

                        override fun onError(throwable: Throwable) {
                            super.onError(throwable)
                            binding.resultTv.text = "语言翻译异常：${throwable.message}"
                        }
                    })
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
            .subscribe(object : Accepter<Int> {
                override fun call(result: Int) {
                    MLog.d(result.toString())
                }

                override fun onStart() {
                    MLog.d("Accepter onStart call")
                }

                override fun onEnd(endState: Accepter.EndState) {
                    MLog.d("Accepter onEnd call:$endState")
                }

                override fun onError(throwable: Throwable) {
                    MLog.d("Accepter onError call :${throwable.message}")
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}