package com.teambind.bind_android.presentation.likedposts

import android.content.Context
import android.content.Intent
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.teambind.bind_android.databinding.ActivityLikedPostsBinding
import com.teambind.bind_android.presentation.base.BaseActivity
import com.teambind.bind_android.presentation.community.adapter.AllPostAdapter
import com.teambind.bind_android.presentation.communitydetail.CommunityDetailActivity
import com.teambind.bind_android.util.extension.collectLatestFlow
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LikedPostsActivity : BaseActivity<ActivityLikedPostsBinding>() {

    private val viewModel: LikedPostsViewModel by viewModels()

    private val postAdapter by lazy {
        AllPostAdapter { article ->
            CommunityDetailActivity.start(this, article.articleId)
        }
    }

    override fun inflateBinding(): ActivityLikedPostsBinding {
        return ActivityLikedPostsBinding.inflate(layoutInflater)
    }

    override fun initView() {
        setupToolbar()
        setupRecyclerView()
    }

    override fun initObserver() {
        collectLatestFlow(viewModel.uiState) { state ->
            binding.layoutLoading.isVisible = state.isLoading && state.posts.isEmpty()
            binding.layoutEmpty.isVisible = !state.isLoading && state.posts.isEmpty()
            binding.rvPosts.isVisible = state.posts.isNotEmpty()

            postAdapter.submitList(state.posts)

            state.errorMessage?.let {
                showToast(it)
                viewModel.clearError()
            }
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        binding.rvPosts.apply {
            adapter = postAdapter
            layoutManager = LinearLayoutManager(this@LikedPostsActivity)

            // 무한 스크롤
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                    val totalItemCount = layoutManager.itemCount

                    if (lastVisibleItem >= totalItemCount - 3) {
                        viewModel.loadMore()
                    }
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, LikedPostsActivity::class.java)
            context.startActivity(intent)
        }
    }
}
