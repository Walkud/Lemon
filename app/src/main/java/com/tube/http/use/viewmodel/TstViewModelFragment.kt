package com.tube.http.use.viewmodel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.tube.http.R
import com.tube.http.databinding.FragmentTstBinding
import com.tube.http.use.BaseFragment
import com.tube.http.use.viewmodel.model.TstViewModel

/**
 * 语言翻译 Fragment
 */
class TstViewModelFragment : BaseFragment() {

    private var _binding: FragmentTstBinding? = null
    private val tstViewModel by viewModels<TstViewModel>()

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
                progressDialog.show()
                tstViewModel.languageTranslation(text)
            } else {
                Toast.makeText(requireContext(), "翻译的文本不能为空!", Toast.LENGTH_SHORT).show()
            }
        }

        tstViewModel.translationResult.observe(viewLifecycleOwner, Observer {
            if (progressDialog.isShowing) {
                progressDialog.dismiss()
            }
            binding.resultTv.text = it
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        tstViewModel.translationResult.removeObservers(viewLifecycleOwner)
    }
}