package com.lemon.http.use.simple

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.lemon.http.R
import com.lemon.http.databinding.FragmentWeatherBinding
import com.lemon.http.net.Net
import com.lemon.http.use.BaseFragment
import kotlinx.coroutines.*

/**
 * 简单使用示例 城市天气预报 Fragment，使用协程进行异步调度
 */
class WeatherFragment : BaseFragment(), CoroutineScope by MainScope() {

    private var _binding: FragmentWeatherBinding? = null
    private val cityCodes =
        listOf(
            "北京:101010100",
            "重庆:101040100",
            "成都:101270101",
            "上海:101020100",
            "海南:101150401",
            "青岛:101120201",
            "大理:101290201"
        )

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var cityCode = "101010100"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentWeatherBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSecond.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }

        binding.queryBtn.setOnClickListener {
            queryCityWeatherInfo()
        }

        val adapter =
            ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, cityCodes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinner.adapter = adapter
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val city = cityCodes[position]
                cityCode = city.split(":")[1]
                queryCityWeatherInfo()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private fun queryCityWeatherInfo() {
        launch {
            progressView.show()
            binding.resultTv.text = try {
                val result = withContext(Dispatchers.IO) {
                    val createTime = System.currentTimeMillis()
                    Net.getWeatherApiService().getCityWeatherInfo(cityCode, createTime)
                }
                Gson().toJson(result)
            } catch (e: Exception) {
                "获取城市天气异常：${e.message}"
            }

            progressView.dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cancel()
    }
}