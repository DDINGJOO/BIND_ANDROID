package com.teambind.bind_android.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.teambind.bind_android.R
import com.teambind.bind_android.databinding.BottomSheetSortingBinding

enum class SortingType(val displayName: String, val apiValue: String) {
    NEAREST("가까운순", "distance"),
    LATEST("최신순", "createdAt")
}

class SortingBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetSortingBinding? = null
    private val binding get() = _binding!!

    private var selectedSorting: SortingType = SortingType.NEAREST
    private var onSortingSelected: ((SortingType) -> Unit)? = null

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetSortingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRadioGroup()
        setupButton()
    }

    private fun setupRadioGroup() {
        // 현재 선택된 정렬 옵션 체크
        val radioButtonId = when (selectedSorting) {
            SortingType.NEAREST -> R.id.rbNearest
            SortingType.LATEST -> R.id.rbLatest
        }
        binding.radioGroupSorting.check(radioButtonId)

        binding.radioGroupSorting.setOnCheckedChangeListener { _, checkedId ->
            selectedSorting = when (checkedId) {
                R.id.rbNearest -> SortingType.NEAREST
                R.id.rbLatest -> SortingType.LATEST
                else -> SortingType.NEAREST
            }
        }
    }

    private fun setupButton() {
        binding.btnApply.setOnClickListener {
            onSortingSelected?.invoke(selectedSorting)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(
            currentSorting: SortingType,
            onSortingSelected: (SortingType) -> Unit
        ): SortingBottomSheet {
            return SortingBottomSheet().apply {
                this.selectedSorting = currentSorting
                this.onSortingSelected = onSortingSelected
            }
        }
    }
}
