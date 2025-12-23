package com.teambind.bind_android.presentation.customerservice

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import com.teambind.bind_android.databinding.ActivityWriteInquiryBinding
import com.teambind.bind_android.presentation.base.BaseActivity
import com.teambind.bind_android.util.extension.collectLatestFlow
import com.teambind.bind_android.util.extension.gone
import com.teambind.bind_android.util.extension.setOnSingleClickListener
import com.teambind.bind_android.util.extension.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WriteInquiryActivity : BaseActivity<ActivityWriteInquiryBinding>() {

    private val viewModel: WriteInquiryViewModel by viewModels()

    override fun inflateBinding(): ActivityWriteInquiryBinding {
        return ActivityWriteInquiryBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        setupToolbar()
        setupSpinner()
        setupInputListeners()
        setupClickListeners()
    }

    override fun initObserver() {
        collectLatestFlow(viewModel.state) { state ->
            when (state) {
                is WriteInquiryState.Idle -> {
                    binding.loadingOverlay.gone()
                }
                is WriteInquiryState.Loading -> {
                    binding.loadingOverlay.visible()
                }
                is WriteInquiryState.Success -> {
                    binding.loadingOverlay.gone()
                    showToast("문의가 접수되었습니다.")
                    setResult(RESULT_OK)
                    finish()
                }
                is WriteInquiryState.Error -> {
                    binding.loadingOverlay.gone()
                    showToast(state.message)
                }
            }
        }

        collectLatestFlow(viewModel.isButtonEnabled) { isEnabled ->
            binding.btnSubmit.isEnabled = isEnabled
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupSpinner() {
        val categories = listOf(
            "문의 유형을 선택해주세요",
            "예약 문의",
            "결제 문의",
            "환불 문의",
            "서비스 이용 문의",
            "기타 문의"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.spinnerCategory.adapter = adapter
    }

    private fun setupInputListeners() {
        binding.etTitle.doAfterTextChanged { text ->
            viewModel.updateTitle(text?.toString() ?: "")
        }

        binding.etContent.doAfterTextChanged { text ->
            viewModel.updateContent(text?.toString() ?: "")
        }
    }

    private fun setupClickListeners() {
        binding.btnSubmit.setOnSingleClickListener {
            val categoryPosition = binding.spinnerCategory.selectedItemPosition
            val category = when (categoryPosition) {
                1 -> "RESERVATION"
                2 -> "PAYMENT"
                3 -> "REFUND"
                4 -> "SERVICE"
                else -> "ETC"
            }
            viewModel.submitInquiry(category)
        }
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, WriteInquiryActivity::class.java)
        }
    }
}
