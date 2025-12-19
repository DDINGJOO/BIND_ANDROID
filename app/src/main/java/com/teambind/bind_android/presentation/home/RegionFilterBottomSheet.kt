package com.teambind.bind_android.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.teambind.bind_android.R
import com.teambind.bind_android.databinding.BottomSheetRegionFilterBinding

class RegionFilterBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetRegionFilterBinding? = null
    private val binding get() = _binding!!

    private var selectedRegion: String? = null
    private var onRegionSelected: ((String?) -> Unit)? = null

    private val regionMap by lazy {
        mapOf(
            binding.chipAll to null,
            binding.chipSeoul to "서울",
            binding.chipGyeonggi to "경기",
            binding.chipIncheon to "인천",
            binding.chipBusan to "부산",
            binding.chipDaegu to "대구",
            binding.chipDaejeon to "대전",
            binding.chipGwangju to "광주"
        )
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetRegionFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupChips()
        setupButtons()
    }

    private fun setupChips() {
        // 현재 선택된 지역 체크
        regionMap.forEach { (chip, region) ->
            if (region == selectedRegion || (selectedRegion == null && region == null)) {
                chip.isChecked = true
            }
        }

        binding.chipGroupRegion.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val checkedChip = binding.root.findViewById<Chip>(checkedIds.first())
                selectedRegion = regionMap[checkedChip]
            }
        }
    }

    private fun setupButtons() {
        // 초기화 버튼
        binding.btnReset.setOnClickListener {
            selectedRegion = null
            binding.chipAll.isChecked = true
        }

        // 적용 버튼
        binding.btnApply.setOnClickListener {
            onRegionSelected?.invoke(selectedRegion)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(
            currentRegion: String?,
            onRegionSelected: (String?) -> Unit
        ): RegionFilterBottomSheet {
            return RegionFilterBottomSheet().apply {
                this.selectedRegion = currentRegion
                this.onRegionSelected = onRegionSelected
            }
        }
    }
}
