package com.teambind.bind_android.presentation.find

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.chip.Chip
import com.teambind.bind_android.databinding.FragmentFindBinding
import com.teambind.bind_android.presentation.base.BaseFragment
import com.teambind.bind_android.util.extension.collectLatestFlow
import com.teambind.bind_android.util.extension.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FindFragment : BaseFragment<FragmentFindBinding>() {

    private val viewModel: FindViewModel by viewModels()
    private var selectedFilterChip: Chip? = null

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentFindBinding {
        return FragmentFindBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        setupFilterChips()
        setupClickListeners()
    }

    override fun initObserver() {
        collectLatestFlow(viewModel.uiState) { state ->
            // 현재 주소 업데이트
            binding.tvLocation.text = state.currentAddress

            // 스튜디오 목록이 있으면 지도에 마커 표시 (Google Maps 연동 필요)
            if (state.studios.isNotEmpty()) {
                // TODO: 지도에 마커 표시
            }

            // 에러 메시지 표시
            state.errorMessage?.let {
                showToast(it)
                viewModel.clearError()
            }
        }
    }

    private fun setupFilterChips() {
        val chips = listOf(
            binding.chipRegion,
            binding.chipSpace,
            binding.chipDateTime,
            binding.chipPersonnel,
            binding.chipKeyword
        )

        chips.forEach { chip ->
            chip.setOnClickListener {
                // Toggle chip selection
                if (selectedFilterChip == chip) {
                    chip.isChecked = false
                    selectedFilterChip = null
                } else {
                    selectedFilterChip?.isChecked = false
                    chip.isChecked = true
                    selectedFilterChip = chip
                    onFilterSelected(chip.text.toString())
                }
            }
        }
    }

    private fun onFilterSelected(filter: String) {
        // TODO: Show filter bottom sheet based on filter type
        showToast("$filter 필터 선택")
    }

    private fun setupClickListeners() {
        with(binding) {
            // Location selector
            layoutLocation.setOnSingleClickListener {
                // TODO: Show location selector
                showToast("위치 선택")
            }

            // Search bar
            searchBar.setOnSingleClickListener {
                // TODO: Navigate to search screen
                showToast("검색 화면으로 이동")
            }

            // Filter button
            btnFilter.setOnSingleClickListener {
                // TODO: Show filter options
                showToast("필터 옵션")
            }

            // Show list button
            btnShowList.setOnSingleClickListener {
                // TODO: Navigate to list view with studios
                val studios = viewModel.uiState.value.studios
                showToast("목록 보기 (${studios.size}개)")
            }

            // Current location button
            fabMyLocation.setOnSingleClickListener {
                // TODO: Move map to current location (GPS 필요)
                showToast("현재 위치로 이동")
                viewModel.loadNearbyStudios()
            }
        }
    }
}
