package com.teambind.bind_android.presentation.reservationoption

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.teambind.bind_android.R
import com.teambind.bind_android.databinding.ActivityReservationOptionBinding
import com.teambind.bind_android.presentation.base.BaseActivity
import com.teambind.bind_android.presentation.reservationform.ReservationFormActivity
import com.teambind.bind_android.presentation.reservationoption.adapter.SelectedProductAdapter
import com.teambind.bind_android.presentation.selecttime.SelectTimeActivity
import com.teambind.bind_android.util.extension.collectLatestFlow
import com.teambind.bind_android.util.extension.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReservationOptionActivity : BaseActivity<ActivityReservationOptionBinding>() {

    private val viewModel: ReservationOptionViewModel by viewModels()

    private val selectedProductAdapter by lazy { SelectedProductAdapter() }

    private val reservationFormLauncher = registerForActivityResult(
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

    override fun inflateBinding(): ActivityReservationOptionBinding {
        return ActivityReservationOptionBinding.inflate(layoutInflater)
    }

    override fun initView() {
        val reservationId = intent.getLongExtra(EXTRA_RESERVATION_ID, 0L)
        val roomId = intent.getLongExtra(EXTRA_ROOM_ID, 0L)
        val placeId = intent.getLongExtra(EXTRA_PLACE_ID, 0L)
        val roomName = intent.getStringExtra(EXTRA_ROOM_NAME) ?: ""
        val roomImageUrl = intent.getStringExtra(EXTRA_ROOM_IMAGE_URL)
        val selectedDate = intent.getStringExtra(EXTRA_SELECTED_DATE) ?: ""
        val selectedTimes = intent.getStringArrayListExtra(EXTRA_SELECTED_TIMES) ?: arrayListOf()
        val minUnit = intent.getIntExtra(EXTRA_MIN_UNIT, 60)
        val roomPrice = intent.getIntExtra(EXTRA_ROOM_PRICE, 0)

        viewModel.initialize(
            reservationId,
            roomId,
            placeId,
            roomName,
            roomImageUrl,
            selectedDate,
            selectedTimes,
            minUnit,
            roomPrice
        )

        setupSelectedProductsRecyclerView()
        setupClickListeners()
    }

    private fun setupSelectedProductsRecyclerView() {
        binding.rvSelectedProducts.apply {
            adapter = selectedProductAdapter
            layoutManager = LinearLayoutManager(this@ReservationOptionActivity)
            itemAnimator = null
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnSingleClickListener {
            showCancelConfirmDialog()
        }

        binding.btnSelectProduct.setOnSingleClickListener {
            viewModel.onSelectProductClick()
        }

        binding.btnConfirm.setOnSingleClickListener {
            viewModel.onConfirmClick()
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
            // Room info
            binding.tvRoomName.text = state.roomName
            if (!state.roomImageUrl.isNullOrEmpty()) {
                Glide.with(this)
                    .load(state.roomImageUrl)
                    .placeholder(R.drawable.bg_circle_gray)
                    .circleCrop()
                    .into(binding.ivRoomImage)
            }

            // Reservation info
            binding.tvDate.text = state.displayDate
            binding.tvTime.text = state.displayTimeRange
            binding.tvPrice.text = state.displayPrice

            // Product option
            binding.tvProductOption.text = state.productOptionText

            // Selected products list
            val hasSelectedProducts = state.selectedProducts.isNotEmpty()
            binding.containerSelectedProducts.isVisible = hasSelectedProducts
            selectedProductAdapter.submitList(state.selectedProducts)

            // Confirm button
            binding.btnConfirm.text = "${state.displayPrice} 예약하기"
            binding.btnConfirm.isEnabled = !state.isLoading

            // Error
            state.errorMessage?.let {
                showToast(it)
                viewModel.clearError()
            }
        }

        collectLatestFlow(viewModel.events) { event ->
            when (event) {
                is ReservationOptionEvent.ShowProductSelector -> {
                    showProductSelectorBottomSheet()
                }

                is ReservationOptionEvent.NavigateToReservationForm -> {
                    ReservationFormActivity.start(
                        launcher = reservationFormLauncher,
                        context = this,
                        reservationId = event.reservationId,
                        roomId = event.roomId,
                        roomName = event.roomName,
                        roomImageUrl = event.roomImageUrl,
                        selectedDate = event.selectedDate,
                        selectedTimes = event.selectedTimes,
                        roomPrice = event.roomPrice,
                        optionPrice = event.optionPrice,
                        totalPrice = event.totalPrice
                    )
                }

                is ReservationOptionEvent.NavigateBack -> {
                    finish()
                }
            }
        }
    }

    private fun showProductSelectorBottomSheet() {
        val currentProducts = viewModel.uiState.value.availableProducts
        val selectedProducts = viewModel.uiState.value.selectedProducts

        ProductSelectorBottomSheet.show(
            fragmentManager = supportFragmentManager,
            availableProducts = currentProducts,
            selectedProducts = selectedProducts,
            onProductsSelected = { products ->
                viewModel.updateSelectedProducts(products)
            }
        )
    }

    override fun onBackPressed() {
        showCancelConfirmDialog()
    }

    companion object {
        private const val EXTRA_RESERVATION_ID = "extra_reservation_id"
        private const val EXTRA_ROOM_ID = "extra_room_id"
        private const val EXTRA_PLACE_ID = "extra_place_id"
        private const val EXTRA_ROOM_NAME = "extra_room_name"
        private const val EXTRA_ROOM_IMAGE_URL = "extra_room_image_url"
        private const val EXTRA_SELECTED_DATE = "extra_selected_date"
        private const val EXTRA_SELECTED_TIMES = "extra_selected_times"
        private const val EXTRA_MIN_UNIT = "extra_min_unit"
        private const val EXTRA_ROOM_PRICE = "extra_room_price"

        fun start(
            launcher: ActivityResultLauncher<Intent>,
            context: Context,
            reservationId: Long,
            roomId: Long,
            placeId: Long,
            roomName: String,
            roomImageUrl: String?,
            selectedDate: String,
            selectedTimes: List<String>,
            minUnit: Int = 60,
            roomPrice: Int = 0
        ) {
            val intent = Intent(context, ReservationOptionActivity::class.java).apply {
                putExtra(EXTRA_RESERVATION_ID, reservationId)
                putExtra(EXTRA_ROOM_ID, roomId)
                putExtra(EXTRA_PLACE_ID, placeId)
                putExtra(EXTRA_ROOM_NAME, roomName)
                putExtra(EXTRA_ROOM_IMAGE_URL, roomImageUrl)
                putExtra(EXTRA_SELECTED_DATE, selectedDate)
                putStringArrayListExtra(EXTRA_SELECTED_TIMES, ArrayList(selectedTimes))
                putExtra(EXTRA_MIN_UNIT, minUnit)
                putExtra(EXTRA_ROOM_PRICE, roomPrice)
            }
            launcher.launch(intent)
        }
    }
}
