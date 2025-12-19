package com.teambind.bind_android.presentation.minifeed

import android.content.Context
import android.content.Intent
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.teambind.bind_android.R
import com.teambind.bind_android.databinding.ActivityMiniFeedBinding
import com.teambind.bind_android.presentation.base.BaseActivity
import com.teambind.bind_android.presentation.communitydetail.CommunityDetailActivity
import com.teambind.bind_android.presentation.start.profilesetting.ProfileSettingActivity
import com.teambind.bind_android.util.extension.collectLatestFlow
import com.teambind.bind_android.util.extension.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MiniFeedActivity : BaseActivity<ActivityMiniFeedBinding>() {

    private val viewModel: MiniFeedViewModel by viewModels()

    private val categoryAdapter by lazy {
        CategoryTagAdapter()
    }

    private val feedAdapter by lazy {
        MiniFeedAdapter { article ->
            CommunityDetailActivity.start(this, article.articleId)
        }
    }

    override fun inflateBinding(): ActivityMiniFeedBinding {
        return ActivityMiniFeedBinding.inflate(layoutInflater)
    }

    override fun initView() {
        val userId = intent.getStringExtra(EXTRA_USER_ID) ?: ""
        viewModel.init(userId)

        setupToolbar()
        setupTabs()
        setupCategoryRecyclerView()
        setupPostsRecyclerView()
    }

    private fun setupToolbar() {
        binding.ivBack.setOnSingleClickListener {
            finish()
        }
    }

    private fun setupTabs() {
        MiniFeedTab.entries.forEach { tab ->
            binding.tabLayout.addTab(
                binding.tabLayout.newTab().setText(tab.displayName)
            )
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.position?.let { position ->
                    viewModel.selectTab(MiniFeedTab.entries[position])
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupCategoryRecyclerView() {
        binding.rvCategories.apply {
            adapter = categoryAdapter
            layoutManager = LinearLayoutManager(
                this@MiniFeedActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        }
    }

    private fun setupPostsRecyclerView() {
        binding.rvPosts.apply {
            adapter = feedAdapter
            layoutManager = LinearLayoutManager(this@MiniFeedActivity)

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5
                        && firstVisibleItemPosition >= 0
                    ) {
                        viewModel.loadMorePosts()
                    }
                }
            })
        }
    }

    private fun setupEditButton() {
        binding.btnEdit.setOnSingleClickListener {
            startActivity(ProfileSettingActivity.newIntent(this, isEditMode = true))
        }
    }

    override fun initObserver() {
        collectLatestFlow(viewModel.uiState) { state ->
            // Profile
            state.profile?.let { profile ->
                // 프로필 이미지
                if (!profile.profileImageUrl.isNullOrEmpty()) {
                    Glide.with(binding.ivProfile)
                        .load(profile.profileImageUrl)
                        .placeholder(R.drawable.bg_circle_gray)
                        .circleCrop()
                        .into(binding.ivProfile)
                } else {
                    binding.ivProfile.setImageResource(R.drawable.bg_circle_gray)
                }

                // 닉네임
                binding.tvNickname.text = "${profile.nickname}님"

                // 소개
                binding.tvIntroduction.text = profile.introduction ?: ""
                binding.tvIntroduction.isVisible = !profile.introduction.isNullOrEmpty()
            }

            // 카테고리 태그
            categoryAdapter.submitList(state.categories)
            binding.rvCategories.isVisible = state.categories.isNotEmpty()

            // 게시글 목록
            feedAdapter.submitList(state.posts)

            // Empty state
            binding.layoutEmpty.isVisible = !state.isLoading && state.posts.isEmpty()
            binding.tvEmptyMessage.text = when (state.selectedTab) {
                MiniFeedTab.POSTS -> "작성한 글이 없습니다."
                MiniFeedTab.COMMENTS -> "댓글단 글이 없습니다."
                MiniFeedTab.LIKES -> "좋아요한 글이 없습니다."
            }

            // Loading
            binding.progressBar.isVisible = state.isLoading && state.posts.isEmpty()

            // Error
            state.errorMessage?.let {
                showToast(it)
                viewModel.clearError()
            }
        }

        // Edit 버튼 설정
        setupEditButton()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshProfile()
    }

    companion object {
        private const val EXTRA_USER_ID = "extra_user_id"

        fun start(context: Context, userId: String) {
            val intent = Intent(context, MiniFeedActivity::class.java).apply {
                putExtra(EXTRA_USER_ID, userId)
            }
            context.startActivity(intent)
        }
    }
}
