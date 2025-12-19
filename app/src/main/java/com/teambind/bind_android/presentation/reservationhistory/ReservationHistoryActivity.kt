package com.teambind.bind_android.presentation.reservationhistory

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.teambind.bind_android.databinding.ActivityReservationHistoryBinding
import com.teambind.bind_android.presentation.base.BaseActivity
import com.teambind.bind_android.presentation.reservationdetail.ReservationDetailActivity
import com.teambind.bind_android.presentation.reservationhistory.adapter.ReservationAdapter
import com.teambind.bind_android.util.extension.collectLatestFlow
import com.teambind.bind_android.util.extension.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReservationHistoryActivity : BaseActivity<ActivityReservationHistoryBinding>() {

    private val viewModel: ReservationHistoryViewModel by viewModels()

    private val detailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Refresh list after cancel/refund
            viewModel.refresh()
        }
    }

    private val reservationAdapter by lazy {
        ReservationAdapter { reservation ->
            ReservationDetailActivity.start(detailLauncher, this, reservation.reservationId)
        }
    }

    override fun inflateBinding(): ActivityReservationHistoryBinding {
        return ActivityReservationHistoryBinding.inflate(layoutInflater)
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
        ReservationTab.entries.forEach { tab ->
            binding.tabLayout.addTab(
                binding.tabLayout.newTab().setText(tab.displayName)
            )
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.position?.let { position ->
                    viewModel.selectTab(ReservationTab.entries[position])
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerView() {
        binding.rvReservations.apply {
            adapter = reservationAdapter
            layoutManager = LinearLayoutManager(this@ReservationHistoryActivity)

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
                        viewModel.loadMoreReservations()
                    }
                }
            })
        }
    }

    override fun initObserver() {
        collectLatestFlow(viewModel.uiState) { state ->
            // Reservations
            reservationAdapter.submitList(state.reservations)

            // Empty state
            binding.layoutEmpty.isVisible = !state.isLoading && state.reservations.isEmpty()
            binding.tvEmptyMessage.text = when (state.selectedTab) {
                ReservationTab.ALL -> "예약 내역이 없습니다"
                ReservationTab.UPCOMING -> "예정된 예약이 없습니다"
                ReservationTab.COMPLETED -> "이용 완료된 예약이 없습니다"
            }

            // Loading
            binding.progressBar.isVisible = state.isLoading && state.reservations.isEmpty()

            // Error
            state.errorMessage?.let {
                showToast(it)
                viewModel.clearError()
            }
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ReservationHistoryActivity::class.java)
            context.startActivity(intent)
        }
    }
}
