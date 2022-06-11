package com.lemon.app.use.space

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.lemon.app.R
import com.lemon.app.databinding.FragmentWeatherBinding
import com.lemon.app.net.Net
import com.lemon.app.use.BaseFragment

/**
 * LemonSpace 使用示例 城市天气预报 Fragment，使用协程进行异步调度
 */
class WeatherLemonSpaceFragment : BaseFragment() {

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
        val createTime = System.currentTimeMillis()
        Net.getWeatherLemonSpaceApiService().getCityWeatherInfo(cityCode, createTime)
            .bindUi(progressView, viewLifecycleOwner)
            .doError { binding.resultTv.text = "获取城市天气异常：${it.message}" }//处理异常错误
            .request {
                binding.resultTv.text = Gson().toJson(it)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}