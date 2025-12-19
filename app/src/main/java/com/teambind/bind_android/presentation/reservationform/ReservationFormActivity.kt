package com.teambind.bind_android.presentation.reservationform

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import com.bumptech.glide.Glide
import com.teambind.bind_android.R
import com.teambind.bind_android.data.model.response.ReservationFieldDto
import com.teambind.bind_android.databinding.ActivityReservationFormBinding
import com.teambind.bind_android.presentation.base.BaseActivity
import com.teambind.bind_android.presentation.reservationcheck.ReservationCheckActivity
import com.teambind.bind_android.presentation.selecttime.SelectTimeActivity
import com.teambind.bind_android.util.extension.collectLatestFlow
import com.teambind.bind_android.util.extension.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReservationFormActivity : BaseActivity<ActivityReservationFormBinding>() {

    private val viewModel: ReservationFormViewModel by viewModels()

    private val fieldEditTexts = mutableMapOf<String, EditText>()

    private val reservationCheckLauncher = registerForActivityResult(
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

    override fun inflateBinding(): ActivityReservationFormBinding {
        return ActivityReservationFormBinding.inflate(layoutInflater)
    }

    override fun initView() {
        val reservationId = intent.getLongExtra(EXTRA_RESERVATION_ID, 0L)
        val roomId = intent.getLongExtra(EXTRA_ROOM_ID, 0L)
        val roomName = intent.getStringExtra(EXTRA_ROOM_NAME) ?: ""
        val roomImageUrl = intent.getStringExtra(EXTRA_ROOM_IMAGE_URL)
        val selectedDate = intent.getStringExtra(EXTRA_SELECTED_DATE) ?: ""
        val selectedTimes = intent.getStringArrayListExtra(EXTRA_SELECTED_TIMES) ?: arrayListOf()
        val roomPrice = intent.getIntExtra(EXTRA_ROOM_PRICE, 0)
        val optionPrice = intent.getIntExtra(EXTRA_OPTION_PRICE, 0)
        val totalPrice = intent.getIntExtra(EXTRA_TOTAL_PRICE, 0)

        viewModel.initialize(
            reservationId, roomId, roomName, roomImageUrl,
            selectedDate, selectedTimes, roomPrice, optionPrice, totalPrice
        )

        setupInputListeners()
        setupClickListeners()
    }

    private fun setupInputListeners() {
        binding.etName.doAfterTextChanged { text ->
            viewModel.updateName(text?.toString() ?: "")
        }

        binding.etContact.doAfterTextChanged { text ->
            viewModel.updateContact(text?.toString() ?: "")
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnSingleClickListener {
            showCancelConfirmDialog()
        }

        binding.btnConfirm.setOnSingleClickListener {
            viewModel.onConfirmClick()
        }

        // Terms checkboxes
        binding.layoutAgreeAll.setOnSingleClickListener {
            viewModel.toggleAllTerms()
        }

        binding.layoutTermsService.setOnSingleClickListener {
            viewModel.toggleTermsService()
        }

        binding.layoutPrivacyPolicy.setOnSingleClickListener {
            viewModel.togglePrivacyPolicy()
        }

        binding.layoutThirdParty.setOnSingleClickListener {
            viewModel.toggleThirdParty()
        }

        binding.layoutCancellationPolicy.setOnSingleClickListener {
            viewModel.toggleCancellationPolicy()
        }

        // FAQ toggle listeners
        setupFaqListeners()
    }

    private fun setupFaqListeners() {
        binding.layoutFaq1.setOnSingleClickListener {
            toggleFaq(binding.layoutFaq1Answer, binding.ivFaq1Arrow)
        }

        binding.layoutFaq2.setOnSingleClickListener {
            toggleFaq(binding.layoutFaq2Answer, binding.ivFaq2Arrow)
        }

        binding.layoutFaq3.setOnSingleClickListener {
            toggleFaq(binding.layoutFaq3Answer, binding.ivFaq3Arrow)
        }

        binding.layoutFaq4.setOnSingleClickListener {
            toggleFaq(binding.layoutFaq4Answer, binding.ivFaq4Arrow)
        }
    }

    private fun toggleFaq(answerLayout: android.view.View, arrowView: android.widget.ImageView) {
        if (answerLayout.visibility == android.view.View.VISIBLE) {
            answerLayout.visibility = android.view.View.GONE
            arrowView.rotation = 0f
        } else {
            answerLayout.visibility = android.view.View.VISIBLE
            arrowView.rotation = 180f
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

    private fun setupDynamicFields(fields: List<ReservationFieldDto>) {
        binding.layoutDynamicFields.removeAllViews()
        fieldEditTexts.clear()

        fields.forEach { field ->
            val fieldView = LayoutInflater.from(this)
                .inflate(R.layout.item_dynamic_field, binding.layoutDynamicFields, false)

            val tvLabel = fieldView.findViewById<TextView>(R.id.tvFieldLabel)
            val etField = fieldView.findViewById<EditText>(R.id.etFieldValue)

            val labelText = if (field.required) "${field.title} *" else field.title
            tvLabel.text = labelText
            etField.hint = "${field.title}을(를) 입력해주세요."

            etField.doAfterTextChanged { text ->
                viewModel.updateAdditionalField(field.title, text?.toString() ?: "")
            }

            fieldEditTexts[field.title] = etField
            binding.layoutDynamicFields.addView(fieldView)
        }
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
            binding.tvRoomPrice.text = state.displayRoomPrice
            binding.tvOptionPrice.text = state.displayOptionPrice

            // Confirm button
            binding.btnConfirm.text = state.displayTotalPrice
            binding.btnConfirm.isEnabled = state.isFormValid && !state.isLoading

            // Terms checkboxes
            binding.ivCheckAll.setImageResource(
                if (state.isAllTermsAgreed) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox_unchecked
            )
            binding.ivCheckTermsService.setImageResource(
                if (state.isTermsServiceAgreed) R.drawable.ic_checkbox_small_checked else R.drawable.ic_checkbox_small_unchecked
            )
            binding.ivCheckPrivacyPolicy.setImageResource(
                if (state.isPrivacyPolicyAgreed) R.drawable.ic_checkbox_small_checked else R.drawable.ic_checkbox_small_unchecked
            )
            binding.ivCheckThirdParty.setImageResource(
                if (state.isThirdPartyAgreed) R.drawable.ic_checkbox_small_checked else R.drawable.ic_checkbox_small_unchecked
            )
            binding.ivCheckCancellationPolicy.setImageResource(
                if (state.isCancellationPolicyAgreed) R.drawable.ic_checkbox_small_checked else R.drawable.ic_checkbox_small_unchecked
            )

            // Dynamic fields
            if (state.dynamicFields.isNotEmpty() && fieldEditTexts.isEmpty()) {
                setupDynamicFields(state.dynamicFields)
            }

            // Error
            state.errorMessage?.let {
                showToast(it)
                viewModel.clearError()
            }
        }

        collectLatestFlow(viewModel.events) { event ->
            when (event) {
                is ReservationFormEvent.NavigateToReservationCheck -> {
                    ReservationCheckActivity.start(
                        launcher = reservationCheckLauncher,
                        context = this,
                        reservationId = event.reservationId,
                        roomName = event.roomName,
                        roomImageUrl = event.roomImageUrl,
                        selectedDate = event.selectedDate,
                        selectedTimes = event.selectedTimes,
                        totalPrice = event.totalPrice
                    )
                }
                is ReservationFormEvent.NavigateBack -> {
                    finish()
                }
                is ReservationFormEvent.ShowError -> {
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
        private const val EXTRA_ROOM_ID = "extra_room_id"
        private const val EXTRA_ROOM_NAME = "extra_room_name"
        private const val EXTRA_ROOM_IMAGE_URL = "extra_room_image_url"
        private const val EXTRA_SELECTED_DATE = "extra_selected_date"
        private const val EXTRA_SELECTED_TIMES = "extra_selected_times"
        private const val EXTRA_ROOM_PRICE = "extra_room_price"
        private const val EXTRA_OPTION_PRICE = "extra_option_price"
        private const val EXTRA_TOTAL_PRICE = "extra_total_price"

        fun start(
            launcher: ActivityResultLauncher<Intent>,
            context: Context,
            reservationId: Long,
            roomId: Long,
            roomName: String,
            roomImageUrl: String?,
            selectedDate: String,
            selectedTimes: List<String>,
            roomPrice: Int,
            optionPrice: Int,
            totalPrice: Int
        ) {
            val intent = Intent(context, ReservationFormActivity::class.java).apply {
                putExtra(EXTRA_RESERVATION_ID, reservationId)
                putExtra(EXTRA_ROOM_ID, roomId)
                putExtra(EXTRA_ROOM_NAME, roomName)
                putExtra(EXTRA_ROOM_IMAGE_URL, roomImageUrl)
                putExtra(EXTRA_SELECTED_DATE, selectedDate)
                putStringArrayListExtra(EXTRA_SELECTED_TIMES, ArrayList(selectedTimes))
                putExtra(EXTRA_ROOM_PRICE, roomPrice)
                putExtra(EXTRA_OPTION_PRICE, optionPrice)
                putExtra(EXTRA_TOTAL_PRICE, totalPrice)
            }
            launcher.launch(intent)
        }
    }
}
