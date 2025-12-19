package com.teambind.bind_android.presentation.payment

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import com.teambind.bind_android.R
import com.teambind.bind_android.databinding.ActivityPaymentBinding
import com.teambind.bind_android.presentation.base.BaseActivity
import com.teambind.bind_android.presentation.selecttime.SelectTimeActivity
import com.teambind.bind_android.util.extension.collectLatestFlow
import com.teambind.bind_android.util.extension.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaymentActivity : BaseActivity<ActivityPaymentBinding>() {

    private val viewModel: PaymentViewModel by viewModels()

    override fun inflateBinding(): ActivityPaymentBinding {
        return ActivityPaymentBinding.inflate(layoutInflater)
    }

    override fun initView() {
        val reservationId = intent.getLongExtra(EXTRA_RESERVATION_ID, 0L)
        val totalPrice = intent.getIntExtra(EXTRA_TOTAL_PRICE, 0)

        viewModel.initialize(reservationId, totalPrice)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnSingleClickListener {
            showCancelConfirmDialog()
        }

        binding.btnTossPay.setOnSingleClickListener {
            viewModel.toggleTossPay()
        }

        binding.layoutConsent1.setOnSingleClickListener {
            viewModel.toggleConsent1()
        }

        binding.layoutConsent2.setOnSingleClickListener {
            viewModel.toggleConsent2()
        }

        binding.layoutConsent3.setOnSingleClickListener {
            viewModel.toggleConsent3()
        }

        binding.btnPay.setOnSingleClickListener {
            viewModel.onPayClick()
        }
    }

    private fun showCancelConfirmDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("예약 취소")
            .setMessage("예약을 취소하시겠습니까?")
            .setPositiveButton("예") { _, _ ->
                // 예약 취소 시 Room Detail까지 돌아감
                setResult(SelectTimeActivity.RESULT_CANCEL_FLOW)
                finish()
            }
            .setNegativeButton("아니오", null)
            .show()
    }

    override fun initObserver() {
        collectLatestFlow(viewModel.uiState) { state ->
            // Price info
            binding.tvRoomPrice.text = state.displayRoomPrice
            binding.tvProductPrice.text = state.displayProductPrice
            binding.tvTotalPrice.text = state.displayTotalPrice

            // TossPay selection
            binding.ivTossPayCheck.setImageResource(
                if (state.isTossPaySelected) R.drawable.ic_checkbox_checked
                else R.drawable.ic_checkbox_unchecked
            )

            // Consent checkboxes
            binding.ivConsent1.setImageResource(
                if (state.consent1Agreed) R.drawable.ic_checkbox_small_checked
                else R.drawable.ic_checkbox_small_unchecked
            )
            binding.ivConsent2.setImageResource(
                if (state.consent2Agreed) R.drawable.ic_checkbox_small_checked
                else R.drawable.ic_checkbox_small_unchecked
            )
            binding.ivConsent3.setImageResource(
                if (state.consent3Agreed) R.drawable.ic_checkbox_small_checked
                else R.drawable.ic_checkbox_small_unchecked
            )

            // Pay button
            binding.btnPay.text = state.displayPaymentButton
            binding.btnPay.isEnabled = state.canProceedPayment && !state.isLoading

            // Error
            state.errorMessage?.let {
                showToast(it)
                viewModel.clearError()
            }
        }

        collectLatestFlow(viewModel.events) { event ->
            when (event) {
                is PaymentEvent.PaymentSuccess -> {
                    showToast("결제가 완료되었습니다.")
                    setResult(Activity.RESULT_OK)
                    finish()
                }

                is PaymentEvent.PaymentFailed -> {
                    showToast(event.message)
                }

                is PaymentEvent.NavigateBack -> {
                    setResult(SelectTimeActivity.RESULT_CANCEL_FLOW)
                    finish()
                }
            }
        }
    }

    override fun onBackPressed() {
        showCancelConfirmDialog()
    }

    companion object {
        private const val EXTRA_RESERVATION_ID = "extra_reservation_id"
        private const val EXTRA_TOTAL_PRICE = "extra_total_price"

        fun start(
            launcher: ActivityResultLauncher<Intent>,
            context: Context,
            reservationId: Long,
            totalPrice: Int
        ) {
            val intent = Intent(context, PaymentActivity::class.java).apply {
                putExtra(EXTRA_RESERVATION_ID, reservationId)
                putExtra(EXTRA_TOTAL_PRICE, totalPrice)
            }
            launcher.launch(intent)
        }
    }
}
