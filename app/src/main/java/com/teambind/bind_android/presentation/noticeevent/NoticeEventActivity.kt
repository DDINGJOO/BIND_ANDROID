package com.teambind.bind_android.presentation.noticeevent

import android.content.Context
import android.content.Intent
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.teambind.bind_android.databinding.ActivityNoticeEventBinding
import com.teambind.bind_android.presentation.base.BaseActivity
import com.teambind.bind_android.presentation.community.adapter.AllPostAdapter
import com.teambind.bind_android.presentation.communitydetail.CommunityDetailActivity
import com.teambind.bind_android.util.extension.collectLatestFlow
import com.teambind.bind_android.util.extension.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NoticeEventActivity : BaseActivity<ActivityNoticeEventBinding>() {

    private val viewModel: NoticeEventViewModel by viewModels()

    private val postAdapter by lazy {
        AllPostAdapter { article ->
            CommunityDetailActivity.start(this, article.articleId)
        }
    }

    override fun inflateBinding(): ActivityNoticeEventBinding {
        return ActivityNoticeEventBinding.inflate(layoutInflater)
    }

    override fun initView() {
        setupToolbar()
        setupTabs()
        setupRecyclerView()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnSingleClickListener {
            finish()
        }
    }

    private fun setupTabs() {
        NoticeEventTab.entries.forEach { tab ->
            binding.tabLayout.addTab(
                binding.tabLayout.newTab().setText(tab.displayName)
            )
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.position?.let { position ->
                    viewModel.selectTab(NoticeEventTab.entries[position])
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerView() {
        binding.rvPosts.apply {
            adapter = postAdapter
            layoutManager = LinearLayoutManager(this@NoticeEventActivity)
        }
    }

    override fun initObserver() {
        collectLatestFlow(viewModel.uiState) { state ->
            // Posts
            postAdapter.submitList(state.posts)

            // Empty state
            binding.layoutEmpty.isVisible = !state.isLoading && state.posts.isEmpty()
            binding.tvEmptyMessage.text = when (state.selectedTab) {
                NoticeEventTab.NOTICE -> "등록된 공지사항이 없습니다"
                NoticeEventTab.EVENT -> "등록된 이벤트가 없습니다"
            }

            // Loading
            binding.progressBar.isVisible = state.isLoading && state.posts.isEmpty()

            // Error
            state.errorMessage?.let {
                showToast(it)
                viewModel.clearError()
            }
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, NoticeEventActivity::class.java)
            context.startActivity(intent)
        }
    }
}
