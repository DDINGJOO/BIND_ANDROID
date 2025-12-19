package com.teambind.bind_android.presentation.paymenthistory

import android.content.Context
import android.content.Intent
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.teambind.bind_android.databinding.ActivityPaymentHistoryBinding
import com.teambind.bind_android.presentation.base.BaseActivity
import com.teambind.bind_android.presentation.paymenthistory.adapter.PaymentHistoryAdapter
import com.teambind.bind_android.util.extension.collectLatestFlow
import com.teambind.bind_android.util.extension.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaymentHistoryActivity : BaseActivity<ActivityPaymentHistoryBinding>() {

    private val viewModel: PaymentHistoryViewModel by viewModels()

    private val paymentAdapter by lazy {
        PaymentHistoryAdapter { payment ->
            showToast("${payment.placeName} - ${payment.displayAmount}")
        }
    }

    override fun inflateBinding(): ActivityPaymentHistoryBinding {
        return ActivityPaymentHistoryBinding.inflate(layoutInflater)
    }

    override fun initView() {
        setupRecyclerView()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        binding.rvPayments.apply {
            adapter = paymentAdapter
            layoutManager = LinearLayoutManager(this@PaymentHistoryActivity)

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
                        viewModel.loadMore()
                    }
                }
            })
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnSingleClickListener {
            finish()
        }
    }

    override fun initObserver() {
        collectLatestFlow(viewModel.uiState) { state ->
            // Payments
            paymentAdapter.submitList(state.payments)

            // Empty state
            binding.layoutEmpty.isVisible = !state.isLoading && state.payments.isEmpty()
            binding.rvPayments.isVisible = state.payments.isNotEmpty()

            // Loading
            binding.progressBar.isVisible = state.isLoading && state.payments.isEmpty()

            // Error
            state.errorMessage?.let {
                showToast(it)
                viewModel.clearError()
            }
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, PaymentHistoryActivity::class.java)
            context.startActivity(intent)
        }
    }
}
