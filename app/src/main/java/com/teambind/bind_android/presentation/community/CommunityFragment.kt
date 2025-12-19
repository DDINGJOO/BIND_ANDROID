package com.teambind.bind_android.presentation.community

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.teambind.bind_android.databinding.FragmentCommunityBinding
import com.teambind.bind_android.presentation.base.BaseFragment
import com.teambind.bind_android.presentation.community.adapter.AllPostAdapter
import com.teambind.bind_android.presentation.communitydetail.CommunityDetailActivity
import com.teambind.bind_android.presentation.home.adapter.HotPostAdapter
import com.teambind.bind_android.presentation.writepost.WritePostActivity
import com.teambind.bind_android.util.extension.collectLatestFlow
import com.teambind.bind_android.util.extension.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CommunityFragment : BaseFragment<FragmentCommunityBinding>() {

    private val viewModel: CommunityViewModel by viewModels()
    private var selectedChip: Chip? = null

    private val hotPostAdapter by lazy {
        HotPostAdapter { article ->
            CommunityDetailActivity.start(requireContext(), article.articleId)
        }
    }

    private val allPostAdapter by lazy {
        AllPostAdapter { article ->
            CommunityDetailActivity.start(requireContext(), article.articleId)
        }
    }

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentCommunityBinding {
        return FragmentCommunityBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        setupCategoryChips()
        setupClickListeners()
        setupHotPosts()
        setupAllPosts()
    }

    override fun initObserver() {
        collectLatestFlow(viewModel.uiState) { state ->
            // 인기 게시글 업데이트
            hotPostAdapter.submitList(state.hotPosts)

            // 전체 게시글 업데이트
            allPostAdapter.submitList(state.allPosts)

            // 정렬 옵션 텍스트 업데이트
            binding.tvSortingOption.text = state.sortBy.displayName

            // 섹션 제목 업데이트 (카테고리에 따라)
            binding.tvSectionTitle.text = state.selectedCategory.sectionTitle

            // 에러 메시지 표시
            state.errorMessage?.let {
                showToast(it)
                viewModel.clearError()
            }
        }
    }

    private fun setupCategoryChips() {
        val chipCategoryMap = mapOf(
            binding.chipAll to CategoryType.ALL,
            binding.chipTip to CategoryType.TIP,
            binding.chipQuestion to CategoryType.QUESTION,
            binding.chipFree to CategoryType.FREE
        )

        // Set initial selection
        selectedChip = binding.chipAll
        binding.chipAll.isChecked = true

        chipCategoryMap.forEach { (chip, category) ->
            chip.setOnClickListener {
                // Deselect previous chip
                selectedChip?.isChecked = false

                // Select new chip
                chip.isChecked = true
                selectedChip = chip

                // Handle category selection
                viewModel.selectCategory(category)
            }
        }
    }

    private fun setupClickListeners() {
        with(binding) {
            // Write button
            fabWrite.setOnSingleClickListener {
                WritePostActivity.start(requireContext())
            }

            // Sorting option
            tvSortingOption.setOnSingleClickListener {
                toggleSortOption()
            }
        }
    }

    private fun toggleSortOption() {
        val currentSort = viewModel.uiState.value.sortBy
        val newSort = if (currentSort == SortType.LATEST) SortType.POPULAR else SortType.LATEST
        viewModel.changeSortType(newSort)
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

    private fun setupAllPosts() {
        binding.rvAllPosts.apply {
            adapter = allPostAdapter
            layoutManager = LinearLayoutManager(requireContext())

            // 페이징을 위한 스크롤 리스너
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    // 마지막 아이템에 가까워지면 더 로드
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5
                        && firstVisibleItemPosition >= 0
                    ) {
                        viewModel.loadMorePosts()
                    }
                }
            })
        }
    }
}
