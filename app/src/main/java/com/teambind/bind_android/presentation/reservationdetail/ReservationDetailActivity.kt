package com.teambind.bind_android.presentation.reservationdetail

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.teambind.bind_android.R
import com.teambind.bind_android.databinding.ActivityReservationDetailBinding
import com.teambind.bind_android.presentation.base.BaseActivity
import com.teambind.bind_android.presentation.studiodetail.StudioDetailActivity
import com.teambind.bind_android.util.extension.collectLatestFlow
import com.teambind.bind_android.util.extension.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReservationDetailActivity : BaseActivity<ActivityReservationDetailBinding>() {

    private val viewModel: ReservationDetailViewModel by viewModels()

    override fun inflateBinding(): ActivityReservationDetailBinding {
        return ActivityReservationDetailBinding.inflate(layoutInflater)
    }

    override fun initView() {
        val reservationId = intent.getLongExtra(EXTRA_RESERVATION_ID, 0L)
        if (reservationId == 0L) {
            showToast("예약 정보를 찾을 수 없습니다.")
            finish()
            return
        }

        viewModel.loadDetail(reservationId)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnSingleClickListener {
            finish()
        }

        binding.btnCancelOrRefund.setOnSingleClickListener {
            showCancelConfirmDialog()
        }

        // 룸 상세 페이지로 이동
        binding.cardRoomInfo.setOnSingleClickListener {
            val roomId = viewModel.uiState.value.roomId
            if (roomId > 0) {
                StudioDetailActivity.start(this, roomId)
            }
        }
    }

    private fun showCancelConfirmDialog() {
        val state = viewModel.uiState.value
        val title = if (state.canRefund) "환불 요청" else "취소 요청"
        val message = if (state.canRefund) {
            "환불을 요청하시겠습니까?\n환불 처리는 영업일 기준 3-5일 소요될 수 있습니다."
        } else {
            "예약을 취소하시겠습니까?"
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("확인") { _, _ ->
                viewModel.onCancelOrRefundClick()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    override fun initObserver() {
        collectLatestFlow(viewModel.uiState) { state ->
            // Loading
            binding.progressBar.isVisible = state.isLoading
            binding.scrollView.isVisible = !state.isLoading

            // Status badge
            binding.tvStatus.text = state.statusDisplay
            updateStatusBadgeColor(state.detail?.status)

            // Place & Room info
            binding.tvPlaceName.text = state.placeName
            binding.tvRoomName.text = state.roomName

            if (!state.roomImageUrl.isNullOrEmpty()) {
                Glide.with(this)
                    .load(state.roomImageUrl)
                    .placeholder(R.drawable.bg_rounded_rect)
                    .centerCrop()
                    .into(binding.ivRoom)
            }

            // Reservation info
            binding.tvDate.text = state.dateDisplay
            binding.tvTime.text = state.timeDisplay

            // Reservist info
            binding.tvReserverName.text = state.reserverName
            binding.tvReserverPhone.text = state.reserverPhone

            // Additional info (추가 정보)
            if (state.additionalInfo.isNotEmpty()) {
                binding.cardAdditionalInfo.isVisible = true
                setupAdditionalInfo(state.additionalInfo)
            } else {
                binding.cardAdditionalInfo.isVisible = false
            }

            // Products (옵션)
            if (state.products.isNotEmpty()) {
                binding.layoutProducts.isVisible = true
                val formatter = java.text.NumberFormat.getNumberInstance(java.util.Locale.KOREA)
                val productText = state.products.joinToString("\n") { product ->
                    val priceText = formatter.format(product.subtotal)
                    "${product.productName} x ${product.quantity}    ${priceText}원"
                }
                binding.tvProducts.text = productText
            } else {
                binding.layoutProducts.isVisible = false
            }

            // Price
            binding.tvRoomPrice.text = state.roomPriceDisplay
            binding.tvOptionPrice.text = state.optionPriceDisplay
            binding.tvTotalPrice.text = state.totalPriceDisplay

            // Cancel/Refund button
            binding.btnCancelOrRefund.isVisible = state.canCancel || state.canRefund
            binding.btnCancelOrRefund.text = state.cancelButtonText

            // Error
            state.errorMessage?.let {
                showToast(it)
                viewModel.clearError()
            }
        }

        collectLatestFlow(viewModel.events) { event ->
            when (event) {
                is ReservationDetailEvent.ShowMessage -> {
                    showToast(event.message)
                }
                is ReservationDetailEvent.NavigateBack -> {
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                is ReservationDetailEvent.RefreshList -> {
                    // Result OK will trigger list refresh
                }
            }
        }
    }

    private fun setupAdditionalInfo(additionalInfo: Map<String, String>) {
        binding.layoutAdditionalInfoItems.removeAllViews()

        additionalInfo.forEach { (key, value) ->
            val itemView = LayoutInflater.from(this)
                .inflate(R.layout.item_additional_info, binding.layoutAdditionalInfoItems, false)

            itemView.findViewById<TextView>(R.id.tvLabel).text = key
            itemView.findViewById<TextView>(R.id.tvValue).text = value

            binding.layoutAdditionalInfoItems.addView(itemView)
        }
    }

    private fun updateStatusBadgeColor(status: String?) {
        val colorRes = when (status) {
            "CONFIRMED" -> R.color.primary_yellow
            "COMPLETED" -> R.color.gray_400
            "CANCELLED", "REJECTED" -> R.color.error_red
            "REFUNDED" -> R.color.gray_500
            else -> R.color.gray_300
        }
        binding.tvStatus.backgroundTintList = getColorStateList(colorRes)
    }

    companion object {
        private const val EXTRA_RESERVATION_ID = "extra_reservation_id"

        fun start(context: Context, reservationId: Long) {
            val intent = Intent(context, ReservationDetailActivity::class.java).apply {
                putExtra(EXTRA_RESERVATION_ID, reservationId)
            }
            context.startActivity(intent)
        }

        fun start(launcher: ActivityResultLauncher<Intent>, context: Context, reservationId: Long) {
            val intent = Intent(context, ReservationDetailActivity::class.java).apply {
                putExtra(EXTRA_RESERVATION_ID, reservationId)
            }
            launcher.launch(intent)
        }
    }
}
