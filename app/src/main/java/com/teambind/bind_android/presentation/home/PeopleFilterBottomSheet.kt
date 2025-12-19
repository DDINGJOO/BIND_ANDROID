package com.teambind.bind_android.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.teambind.bind_android.R
import com.teambind.bind_android.databinding.BottomSheetPeopleFilterBinding

class PeopleFilterBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetPeopleFilterBinding? = null
    private val binding get() = _binding!!

    private var currentCount: Int = 1
    private var onCountSelected: ((Int?) -> Unit)? = null

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetPeopleFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCounter()
        setupButtons()
    }

    private fun setupCounter() {
        updateCountDisplay()

        binding.btnMinus.setOnClickListener {
            if (currentCount > 1) {
                currentCount--
                updateCountDisplay()
            }
        }

        binding.btnPlus.setOnClickListener {
            if (currentCount < 20) {
                currentCount++
                updateCountDisplay()
            }
        }
    }

    private fun updateCountDisplay() {
        binding.tvCount.text = currentCount.toString()
    }

    private fun setupButtons() {
        // 초기화 버튼
        binding.btnReset.setOnClickListener {
            currentCount = 1
            updateCountDisplay()
        }

        // 적용 버튼
        binding.btnApply.setOnClickListener {
            onCountSelected?.invoke(currentCount)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(
            currentCount: Int?,
            onCountSelected: (Int?) -> Unit
        ): PeopleFilterBottomSheet {
            return PeopleFilterBottomSheet().apply {
                this.currentCount = currentCount ?: 1
                this.onCountSelected = onCountSelected
            }
        }
    }
}
