package com.teambind.bind_android.presentation.reservationcancel

import android.content.Context
import android.content.Intent
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.teambind.bind_android.databinding.ActivityReservationCancelBinding
import com.teambind.bind_android.presentation.base.BaseActivity
import com.teambind.bind_android.util.extension.collectLatestFlow
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReservationCancelActivity : BaseActivity<ActivityReservationCancelBinding>() {

    private val viewModel: ReservationCancelViewModel by viewModels()

    override fun inflateBinding(): ActivityReservationCancelBinding {
        return ActivityReservationCancelBinding.inflate(layoutInflater)
    }

    override fun initView() {
        val reservationId = intent.getLongExtra(EXTRA_RESERVATION_ID, -1)
        if (reservationId == -1L) {
            showToast("잘못된 접근입니다.")
            finish()
            return
        }

        setupToolbar()
        setupClickListeners()
        viewModel.loadReservationDetail(reservationId)
    }

    override fun initObserver() {
        collectLatestFlow(viewModel.uiState) { state ->
            binding.layoutLoading.isVisible = state.isLoading

            // 금액 정보 업데이트
            binding.tvPaymentAmount.text = formatPrice(state.totalPrice)
            binding.tvCancelFeeAmount.text = formatPrice(state.cancelFee)
            binding.tvCancelFee.text = "-${formatPrice(state.cancelFee)}"
            binding.tvRefundAmount.text = formatPrice(state.refundAmount)
            binding.tvCancelFeePolicy.text = state.cancelFeePolicy

            // 취소 버튼 상태
            binding.btnCancel.isEnabled = state.canCancel
            binding.btnCancel.alpha = if (state.canCancel) 1f else 0.5f

            // 에러 메시지
            state.errorMessage?.let {
                showToast(it)
            }
        }

        collectLatestFlow(viewModel.event) { event ->
            when (event) {
                is ReservationCancelEvent.CancelSuccess -> {
                    showToast("예약이 취소되었습니다.")
                    setResult(RESULT_OK)
                    finish()
                }
                is ReservationCancelEvent.CancelError -> {
                    showToast(event.message)
                }
                is ReservationCancelEvent.ShowConfirmDialog -> {
                    showConfirmDialog(event.message)
                }
            }
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        binding.btnCancel.setOnClickListener {
            viewModel.onCancelClick()
        }
    }

    private fun showConfirmDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("예약 취소")
            .setMessage(message)
            .setPositiveButton("취소하기") { _, _ ->
                viewModel.confirmCancel()
            }
            .setNegativeButton("돌아가기", null)
            .show()
    }

    private fun formatPrice(price: Int): String {
        return String.format("%,d원", price)
    }

    companion object {
        private const val EXTRA_RESERVATION_ID = "reservation_id"

        fun start(context: Context, reservationId: Long) {
            val intent = Intent(context, ReservationCancelActivity::class.java).apply {
                putExtra(EXTRA_RESERVATION_ID, reservationId)
            }
            context.startActivity(intent)
        }

        fun createIntent(context: Context, reservationId: Long): Intent {
            return Intent(context, ReservationCancelActivity::class.java).apply {
                putExtra(EXTRA_RESERVATION_ID, reservationId)
            }
        }
    }
}
