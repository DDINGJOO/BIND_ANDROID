package com.teambind.bind_android.presentation.selecttime

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.teambind.bind_android.R
import com.teambind.bind_android.databinding.ActivitySelectTimeBinding
import com.teambind.bind_android.presentation.base.BaseActivity
import com.teambind.bind_android.presentation.reservationoption.ReservationOptionActivity
import com.teambind.bind_android.presentation.selecttime.adapter.TimeSlotAdapter
import com.teambind.bind_android.util.extension.collectLatestFlow
import com.teambind.bind_android.util.extension.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class SelectTimeActivity : BaseActivity<ActivitySelectTimeBinding>() {

    private val viewModel: SelectTimeViewModel by viewModels()

    private val timeSlotAdapter by lazy {
        TimeSlotAdapter { index, _ ->
            viewModel.onTimeSlotClick(index)
        }
    }

    private val reservationOptionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                setResult(Activity.RESULT_OK)
                finish()
            }
            RESULT_CANCEL_FLOW -> {
                // 예약 취소 시 Room Detail까지 돌아감
                setResult(RESULT_CANCEL_FLOW)
                finish()
            }
        }
    }

    override fun inflateBinding(): ActivitySelectTimeBinding {
        return ActivitySelectTimeBinding.inflate(layoutInflater)
    }

    override fun initView() {
        window.setBackgroundDrawableResource(android.R.color.transparent)

        val roomId = intent.getLongExtra(EXTRA_ROOM_ID, 0L)
        val placeId = intent.getLongExtra(EXTRA_PLACE_ID, 0L)
        val roomName = intent.getStringExtra(EXTRA_ROOM_NAME) ?: ""
        val roomImageUrl = intent.getStringExtra(EXTRA_ROOM_IMAGE_URL)
        val selectedDate = intent.getStringExtra(EXTRA_SELECTED_DATE) ?: ""
        val minUnit = intent.getIntExtra(EXTRA_MIN_UNIT, 60)

        viewModel.initialize(roomId, placeId, roomName, roomImageUrl, selectedDate, minUnit)

        setupRecyclerView()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        binding.rvTimeSlots.apply {
            adapter = timeSlotAdapter
            layoutManager = GridLayoutManager(this@SelectTimeActivity, 4)
            itemAnimator = null
        }
    }

    private fun setupClickListeners() {
        binding.btnClose.setOnSingleClickListener {
            viewModel.onCloseClick()
        }

        binding.btnConfirm.setOnSingleClickListener {
            viewModel.onConfirmClick()
        }

        binding.root.setOnClickListener {
            finish()
        }

        binding.bottomSheetCard.setOnClickListener {
            // Consume click to prevent dismissing when clicking on bottom sheet
        }
    }

    override fun initObserver() {
        collectLatestFlow(viewModel.uiState) { state ->
            // Date
            binding.tvSelectedDate.text = state.displayDate

            // Room info
            binding.tvRoomName.text = state.roomName
            binding.tvTimeGuide.text = "최소 ${state.minUnit}분 단위로 선택"

            if (!state.roomImageUrl.isNullOrEmpty()) {
                Glide.with(this)
                    .load(state.roomImageUrl)
                    .placeholder(R.drawable.bg_rounded_rect)
                    .centerCrop()
                    .into(binding.ivRoomImage)
            }

            // Time slots
            timeSlotAdapter.submitList(state.timeSlots)

            // Selected time range
            binding.tvSelectedTime.text = state.selectedTimeRange
            binding.tvSelectedTime.isVisible = state.selectedTimeRange.isNotEmpty()

            // Confirm button
            binding.btnConfirm.isEnabled = state.canConfirm
            val formatter = NumberFormat.getNumberInstance(Locale.KOREA)
            binding.btnConfirm.text = if (state.totalPrice > 0) {
                "${formatter.format(state.totalPrice)}원 예약 진행"
            } else {
                "예약 진행"
            }

            // Error
            state.errorMessage?.let {
                showToast(it)
                viewModel.clearError()
            }
        }

        collectLatestFlow(viewModel.events) { event ->
            when (event) {
                is SelectTimeEvent.NavigateToReservationOption -> {
                    ReservationOptionActivity.start(
                        launcher = reservationOptionLauncher,
                        context = this,
                        reservationId = event.reservationId,
                        roomId = event.roomId,
                        placeId = event.placeId,
                        roomName = event.roomName,
                        roomImageUrl = event.roomImageUrl,
                        selectedDate = event.selectedDate,
                        selectedTimes = event.selectedTimes,
                        minUnit = event.minUnit,
                        roomPrice = event.roomPrice
                    )
                }
                is SelectTimeEvent.Dismiss -> {
                    finish()
                }
                is SelectTimeEvent.ShowUnavailableAlert -> {
                    showToast("선택 범위 내에 예약 불가능한 시간이 포함되어 있습니다.")
                }
            }
        }
    }

    companion object {
        const val RESULT_CANCEL_FLOW = 100

        private const val EXTRA_ROOM_ID = "extra_room_id"
        private const val EXTRA_PLACE_ID = "extra_place_id"
        private const val EXTRA_ROOM_NAME = "extra_room_name"
        private const val EXTRA_ROOM_IMAGE_URL = "extra_room_image_url"
        private const val EXTRA_SELECTED_DATE = "extra_selected_date"
        private const val EXTRA_MIN_UNIT = "extra_min_unit"

        fun start(
            context: Context,
            roomId: Long,
            placeId: Long,
            roomName: String,
            roomImageUrl: String?,
            selectedDate: String,
            minUnit: Int
        ) {
            val intent = Intent(context, SelectTimeActivity::class.java).apply {
                putExtra(EXTRA_ROOM_ID, roomId)
                putExtra(EXTRA_PLACE_ID, placeId)
                putExtra(EXTRA_ROOM_NAME, roomName)
                putExtra(EXTRA_ROOM_IMAGE_URL, roomImageUrl)
                putExtra(EXTRA_SELECTED_DATE, selectedDate)
                putExtra(EXTRA_MIN_UNIT, minUnit)
            }
            context.startActivity(intent)
        }

        fun startForResult(
            launcher: androidx.activity.result.ActivityResultLauncher<Intent>,
            context: Context,
            roomId: Long,
            placeId: Long,
            roomName: String,
            roomImageUrl: String?,
            selectedDate: String,
            minUnit: Int
        ) {
            val intent = Intent(context, SelectTimeActivity::class.java).apply {
                putExtra(EXTRA_ROOM_ID, roomId)
                putExtra(EXTRA_PLACE_ID, placeId)
                putExtra(EXTRA_ROOM_NAME, roomName)
                putExtra(EXTRA_ROOM_IMAGE_URL, roomImageUrl)
                putExtra(EXTRA_SELECTED_DATE, selectedDate)
                putExtra(EXTRA_MIN_UNIT, minUnit)
            }
            launcher.launch(intent)
        }
    }
}
