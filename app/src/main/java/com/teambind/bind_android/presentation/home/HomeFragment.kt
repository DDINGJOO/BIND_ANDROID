package com.teambind.bind_android.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.teambind.bind_android.databinding.FragmentHomeBinding
import com.teambind.bind_android.presentation.base.BaseFragment
import com.teambind.bind_android.presentation.communitydetail.CommunityDetailActivity
import com.teambind.bind_android.presentation.home.adapter.BannerAdapter
import com.teambind.bind_android.presentation.home.adapter.HomeStudioAdapter
import com.teambind.bind_android.presentation.home.adapter.HotPostAdapter
import com.teambind.bind_android.presentation.reservationhistory.ReservationHistoryActivity
import com.teambind.bind_android.presentation.search.SearchActivity
import com.teambind.bind_android.presentation.studiodetail.StudioDetailActivity
import com.teambind.bind_android.util.extension.collectLatestFlow
import com.teambind.bind_android.util.extension.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    private val viewModel: HomeViewModel by viewModels()

    private val hotPostAdapter by lazy {
        HotPostAdapter { article ->
            CommunityDetailActivity.start(requireContext(), article.articleId)
        }
    }

    private val studioAdapter by lazy {
        HomeStudioAdapter { studio ->
            // Place ID로 상세 화면 이동
            val placeId = studio.studioId
            if (!placeId.isNullOrEmpty()) {
                StudioDetailActivity.startWithPlace(requireContext(), placeId)
            }
        }
    }

    private val bannerAdapter by lazy { BannerAdapter() }

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        setupBanner()
        setupClickListeners()
        setupHotPosts()
        setupPlaces()
    }

    override fun initObserver() {
        collectLatestFlow(viewModel.uiState) { state ->
            // 인기 게시글 업데이트
            hotPostAdapter.submitList(state.hotPosts)

            // 스튜디오 목록 업데이트
            studioAdapter.submitList(state.studios)

            // 필터 칩 업데이트
            updateFilterChips(state.filter)

            // 에러 메시지 표시
            state.errorMessage?.let {
                showToast(it)
                viewModel.clearError()
            }
        }
    }

    private fun updateFilterChips(filter: HomeFilter) {
        with(binding) {
            // 지역 칩
            chipRegion.text = filter.province ?: "지역"
            chipRegion.isChecked = filter.province != null

            // 날짜/시간 칩
            val hasDateTime = filter.date != null || filter.startTime != null
            chipDateTime.text = when {
                filter.date != null && filter.startTime != null -> {
                    val datePart = filter.date.substring(5).replace("-", "/")
                    val startHour = filter.startTime.substringBefore(":").toIntOrNull() ?: 0
                    val endHour = filter.endTime?.substringBefore(":")?.toIntOrNull() ?: 24
                    "${datePart} ${startHour}~${endHour}시"
                }
                filter.date != null -> filter.date.substring(5).replace("-", "/")
                filter.startTime != null -> {
                    val startHour = filter.startTime.substringBefore(":").toIntOrNull() ?: 0
                    val endHour = filter.endTime?.substringBefore(":")?.toIntOrNull() ?: 24
                    "${startHour}~${endHour}시"
                }
                else -> "날짜/시간"
            }
            chipDateTime.isChecked = hasDateTime

            // 인원 칩
            chipPeople.text = if (filter.headCount != null) {
                "${filter.headCount}명"
            } else "인원"
            chipPeople.isChecked = filter.headCount != null

            // 키워드 칩
            val keywordCount = filter.keywordIds?.split(",")?.filter { it.isNotEmpty() }?.size ?: 0
            chipKeyword.text = if (keywordCount > 0) "키워드 ${keywordCount}개" else "키워드"
            chipKeyword.isChecked = keywordCount > 0
        }
    }

    private fun setupClickListeners() {
        with(binding) {
            // 검색 버튼
            btnSearch.setOnSingleClickListener {
                SearchActivity.start(requireContext())
            }

            // 내 주변 카드
            cardNearby.setOnSingleClickListener {
                showToast("내 주변 스튜디오")
            }

            // 내 예약 카드
            cardReservation.setOnSingleClickListener {
                ReservationHistoryActivity.start(requireContext())
            }

            // 필터 버튼 - 모든 필터 초기화
            btnFilter.setOnSingleClickListener {
                viewModel.clearAllFilters()
                showToast("필터가 초기화되었습니다")
            }

            // 지역 필터 칩
            chipRegion.setOnSingleClickListener {
                showRegionFilterBottomSheet()
            }

            // 날짜/시간 필터 칩
            chipDateTime.setOnSingleClickListener {
                showDateTimeFilterBottomSheet()
            }

            // 인원 필터 칩
            chipPeople.setOnSingleClickListener {
                showPeopleFilterBottomSheet()
            }

            // 키워드 필터 칩
            chipKeyword.setOnSingleClickListener {
                showKeywordFilterBottomSheet()
            }
        }
    }

    private fun showRegionFilterBottomSheet() {
        val currentRegion = viewModel.uiState.value.filter.province
        RegionFilterBottomSheet.newInstance(currentRegion) { selectedRegion ->
            viewModel.updateRegionFilter(selectedRegion)
        }.show(childFragmentManager, "RegionFilter")
    }

    private fun showDateTimeFilterBottomSheet() {
        val filter = viewModel.uiState.value.filter
        DateTimeFilterBottomSheet.newInstance(
            currentDate = filter.date,
            currentStartTime = filter.startTime,
            currentEndTime = filter.endTime
        ) { selection ->
            viewModel.updateDateTimeFilter(selection.date, selection.startTime, selection.endTime)
        }.show(childFragmentManager, "DateTimeFilter")
    }

    private fun showPeopleFilterBottomSheet() {
        val currentCount = viewModel.uiState.value.filter.headCount
        PeopleFilterBottomSheet.newInstance(currentCount) { selectedCount ->
            viewModel.updateHeadCountFilter(selectedCount)
        }.show(childFragmentManager, "PeopleFilter")
    }

    private fun showKeywordFilterBottomSheet() {
        val currentKeywordIds = viewModel.uiState.value.filter.keywordIds
            ?.split(",")
            ?.mapNotNull { it.toLongOrNull() }
        KeywordFilterBottomSheet.newInstance(currentKeywordIds) { selectedIds ->
            val keywordIdsString = if (selectedIds.isEmpty()) null else selectedIds.joinToString(",")
            viewModel.updateKeywordFilter(keywordIdsString)
        }.show(childFragmentManager, "KeywordFilter")
    }

    private fun setupBanner() {
        binding.vpBanner.adapter = bannerAdapter
    }

    private fun setupHotPosts() {
        binding.rvHotPosts.apply {
            adapter = hotPostAdapter
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
        }
    }

    private fun setupPlaces() {
        binding.rvPlaces.apply {
            adapter = studioAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }
}
