package com.teambind.bind_android.presentation.search

import android.content.Context
import android.content.Intent
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.teambind.bind_android.databinding.ActivitySearchBinding
import com.teambind.bind_android.presentation.base.BaseActivity
import com.teambind.bind_android.presentation.search.adapter.RecentSearchAdapter
import com.teambind.bind_android.presentation.search.adapter.SearchResultAdapter
import com.teambind.bind_android.presentation.studiodetail.StudioDetailActivity
import com.teambind.bind_android.util.extension.collectLatestFlow
import com.teambind.bind_android.util.extension.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchActivity : BaseActivity<ActivitySearchBinding>() {

    private val viewModel: SearchViewModel by viewModels()

    private val recentSearchAdapter by lazy {
        RecentSearchAdapter(
            onItemClick = { query -> viewModel.onRecentSearchClick(query) },
            onDeleteClick = { query -> viewModel.removeRecentSearch(query) }
        )
    }

    private val searchResultAdapter by lazy {
        SearchResultAdapter { place ->
            viewModel.onPlaceClick(place.placeId)
        }
    }

    override fun inflateBinding(): ActivitySearchBinding {
        return ActivitySearchBinding.inflate(layoutInflater)
    }

    override fun initView() {
        setupRecyclerViews()
        setupInputListeners()
        setupClickListeners()
    }

    private fun setupRecyclerViews() {
        binding.rvRecentSearches.apply {
            adapter = recentSearchAdapter
            layoutManager = LinearLayoutManager(this@SearchActivity)
        }

        binding.rvSearchResults.apply {
            adapter = searchResultAdapter
            layoutManager = LinearLayoutManager(this@SearchActivity)
        }
    }

    private fun setupInputListeners() {
        binding.etSearch.doAfterTextChanged { text ->
            viewModel.updateQuery(text?.toString() ?: "")
        }

        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.search()
                true
            } else {
                false
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnSingleClickListener {
            finish()
        }

        binding.btnClearAll.setOnSingleClickListener {
            viewModel.clearAllRecentSearches()
        }
    }

    override fun initObserver() {
        collectLatestFlow(viewModel.uiState) { state ->
            // Recent searches
            recentSearchAdapter.submitList(state.recentSearches)
            binding.layoutRecentSearches.isVisible = !state.isSearching && state.recentSearches.isNotEmpty()

            // Search results
            searchResultAdapter.submitList(state.searchResults)
            binding.rvSearchResults.isVisible = state.isSearching && state.searchResults.isNotEmpty()

            // Empty state
            binding.layoutEmpty.isVisible = state.showNoResults

            // Loading
            binding.progressBar.isVisible = state.isLoading

            // Error
            state.errorMessage?.let {
                showToast(it)
                viewModel.clearError()
            }
        }

        collectLatestFlow(viewModel.events) { event ->
            when (event) {
                is SearchEvent.NavigateToDetail -> {
                    StudioDetailActivity.start(this, event.placeId)
                }
            }
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, SearchActivity::class.java)
            context.startActivity(intent)
        }
    }
}
