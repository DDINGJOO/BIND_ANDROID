package com.teambind.bind_android.presentation.myposts

import android.content.Context
import android.content.Intent
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.teambind.bind_android.databinding.ActivityMyPostsBinding
import com.teambind.bind_android.presentation.base.BaseActivity
import com.teambind.bind_android.presentation.communitydetail.CommunityDetailActivity
import com.teambind.bind_android.util.extension.collectLatestFlow
import com.teambind.bind_android.util.extension.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyPostsActivity : BaseActivity<ActivityMyPostsBinding>() {

    private val viewModel: MyPostsViewModel by viewModels()

    private val postAdapter by lazy {
        MyPostCardAdapter { article ->
            CommunityDetailActivity.start(this, article.articleId)
        }
    }

    override fun inflateBinding(): ActivityMyPostsBinding {
        return ActivityMyPostsBinding.inflate(layoutInflater)
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
        MyPostsTab.entries.forEach { tab ->
            binding.tabLayout.addTab(
                binding.tabLayout.newTab().setText(tab.displayName)
            )
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.position?.let { position ->
                    viewModel.selectTab(MyPostsTab.entries[position])
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerView() {
        binding.rvPosts.apply {
            adapter = postAdapter
            layoutManager = LinearLayoutManager(this@MyPostsActivity)

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

    override fun initObserver() {
        collectLatestFlow(viewModel.uiState) { state ->
            // Posts
            postAdapter.submitList(state.posts)

            // Empty state
            binding.layoutEmpty.isVisible = !state.isLoading && state.posts.isEmpty()
            binding.tvEmptyMessage.text = when (state.selectedTab) {
                MyPostsTab.WRITTEN -> "작성한 게시글이 없습니다"
                MyPostsTab.COMMENTED -> "댓글을 작성한 게시글이 없습니다"
                MyPostsTab.LIKED -> "좋아요한 게시글이 없습니다"
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
            val intent = Intent(context, MyPostsActivity::class.java)
            context.startActivity(intent)
        }
    }
}
