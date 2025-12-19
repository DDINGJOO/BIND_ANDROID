package com.teambind.bind_android.presentation.reservationcheck

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.teambind.bind_android.R
import com.teambind.bind_android.databinding.ActivityReservationCheckBinding
import com.teambind.bind_android.presentation.base.BaseActivity
import com.teambind.bind_android.presentation.payment.PaymentActivity
import com.teambind.bind_android.presentation.selecttime.SelectTimeActivity
import com.teambind.bind_android.util.extension.collectLatestFlow
import com.teambind.bind_android.util.extension.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReservationCheckActivity : BaseActivity<ActivityReservationCheckBinding>() {

    private val viewModel: ReservationCheckViewModel by viewModels()

    private val paymentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                setResult(Activity.RESULT_OK)
                finish()
            }

            SelectTimeActivity.RESULT_CANCEL_FLOW -> {
                // 예약 취소 시 Room Detail까지 돌아감
                setResult(SelectTimeActivity.RESULT_CANCEL_FLOW)
                finish()
            }
        }
    }

    override fun inflateBinding(): ActivityReservationCheckBinding {
        return ActivityReservationCheckBinding.inflate(layoutInflater)
    }

    override fun initView() {
        window.setBackgroundDrawableResource(android.R.color.transparent)

        val reservationId = intent.getLongExtra(EXTRA_RESERVATION_ID, 0L)
        val roomName = intent.getStringExtra(EXTRA_ROOM_NAME) ?: ""
        val roomImageUrl = intent.getStringExtra(EXTRA_ROOM_IMAGE_URL)
        val selectedDate = intent.getStringExtra(EXTRA_SELECTED_DATE) ?: ""
        val selectedTimes = intent.getStringArrayListExtra(EXTRA_SELECTED_TIMES) ?: arrayListOf()
        val totalPrice = intent.getIntExtra(EXTRA_TOTAL_PRICE, 0)

        viewModel.initialize(
            reservationId = reservationId,
            roomName = roomName,
            roomImageUrl = roomImageUrl,
            selectedDate = selectedDate,
            selectedTimes = selectedTimes,
            totalPrice = totalPrice
        )

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnClose.setOnSingleClickListener {
            showCancelConfirmDialog()
        }

        binding.btnConfirm.setOnSingleClickListener {
            viewModel.onConfirmClick()
        }

        binding.root.setOnClickListener {
            // Only close if clicked outside the bottom sheet
            showCancelConfirmDialog()
        }

        binding.bottomSheetCard.setOnClickListener {
            // Consume click to prevent closing
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
            // Studio info
            binding.tvStudioName.text = state.studioName
            binding.tvStudioAddress.text = state.studioAddress

            if (!state.studioImageUrl.isNullOrEmpty()) {
                Glide.with(this)
                    .load(state.studioImageUrl)
                    .placeholder(R.drawable.bg_rounded_rect)
                    .centerCrop()
                    .into(binding.ivStudio)
            }

            // Reservation info
            binding.tvReservationDate.text = state.reservationDate
            binding.tvReservationTime.text = state.reservationTime
            binding.tvProductOptions.text = state.productOptions

            // Reservist info
            binding.tvReservistName.text = state.reservistName
            binding.tvReservistContact.text = state.reservistContact

            // Price info
            binding.tvRoomPrice.text = state.roomPrice
            binding.tvOptionPrice.text = state.optionPrice

            // Confirm button
            binding.btnConfirm.text = state.displayTotalPrice

            // Error
            state.errorMessage?.let {
                showToast(it)
                viewModel.clearError()
            }
        }

        collectLatestFlow(viewModel.events) { event ->
            when (event) {
                is ReservationCheckEvent.NavigateToPayment -> {
                    PaymentActivity.start(
                        launcher = paymentLauncher,
                        context = this,
                        reservationId = event.reservationId,
                        totalPrice = event.totalPrice
                    )
                }

                is ReservationCheckEvent.Dismiss -> {
                    finish()
                }

                is ReservationCheckEvent.ShowError -> {
                    showToast(event.message)
                }
            }
        }
    }

    override fun onBackPressed() {
        showCancelConfirmDialog()
    }

    companion object {
        private const val EXTRA_RESERVATION_ID = "extra_reservation_id"
        private const val EXTRA_ROOM_NAME = "extra_room_name"
        private const val EXTRA_ROOM_IMAGE_URL = "extra_room_image_url"
        private const val EXTRA_SELECTED_DATE = "extra_selected_date"
        private const val EXTRA_SELECTED_TIMES = "extra_selected_times"
        private const val EXTRA_TOTAL_PRICE = "extra_total_price"

        fun start(
            launcher: ActivityResultLauncher<Intent>,
            context: Context,
            reservationId: Long,
            roomName: String,
            roomImageUrl: String?,
            selectedDate: String,
            selectedTimes: List<String>,
            totalPrice: Int
        ) {
            val intent = Intent(context, ReservationCheckActivity::class.java).apply {
                putExtra(EXTRA_RESERVATION_ID, reservationId)
                putExtra(EXTRA_ROOM_NAME, roomName)
                putExtra(EXTRA_ROOM_IMAGE_URL, roomImageUrl)
                putExtra(EXTRA_SELECTED_DATE, selectedDate)
                putStringArrayListExtra(EXTRA_SELECTED_TIMES, ArrayList(selectedTimes))
                putExtra(EXTRA_TOTAL_PRICE, totalPrice)
            }
            launcher.launch(intent)
        }
    }
}
