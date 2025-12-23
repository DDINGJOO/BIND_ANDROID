package com.teambind.bind_android.presentation.start.auth

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.teambind.bind_android.databinding.ActivityIdVerifyBinding
import com.teambind.bind_android.presentation.base.BaseActivity
import com.teambind.bind_android.util.extension.collectLatestFlow
import com.teambind.bind_android.util.extension.gone
import com.teambind.bind_android.util.extension.setOnSingleClickListener
import com.teambind.bind_android.util.extension.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IDVerifyActivity : BaseActivity<ActivityIdVerifyBinding>() {

    private val viewModel: IDVerifyViewModel by viewModels()

    override fun inflateBinding(): ActivityIdVerifyBinding {
        return ActivityIdVerifyBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        setupToolbar()
        setupInputListeners()
        setupClickListeners()
    }

    override fun initObserver() {
        collectLatestFlow(viewModel.state) { state ->
            when (state) {
                is IDVerifyState.Idle -> {
                    binding.loadingOverlay.gone()
                }

                is IDVerifyState.Loading -> {
                    binding.loadingOverlay.visible()
                }

                is IDVerifyState.CodeSent -> {
                    binding.loadingOverlay.gone()
                    showCodeInputSection()
                }

                is IDVerifyState.Success -> {
                    binding.loadingOverlay.gone()
                    showToast("본인인증이 완료되었습니다.")
                    setResult(RESULT_OK)
                    finish()
                }

                is IDVerifyState.Error -> {
                    binding.loadingOverlay.gone()
                    showToast(state.message)
                    viewModel.clearError()
                }
            }
        }

        collectLatestFlow(viewModel.isRequestButtonEnabled) { isEnabled ->
            binding.btnRequestCode.isEnabled = isEnabled
        }

        collectLatestFlow(viewModel.isVerifyButtonEnabled) { isEnabled ->
            binding.btnVerify.isEnabled = isEnabled
        }

        collectLatestFlow(viewModel.remainingSeconds) { seconds ->
            binding.tvTimer.text = "남은 시간: ${viewModel.formatTime(seconds)}"
            binding.tilCode.suffixText = viewModel.formatTime(seconds)
        }

        collectLatestFlow(viewModel.isTimerRunning) { isRunning ->
            binding.btnRequestCode.isEnabled = !isRunning && viewModel.phoneNumber.value.length >= 10
            // 타이머 실행 중일 때 휴대폰 번호 수정 방지
            binding.etPhone.isEnabled = !isRunning
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupInputListeners() {
        with(binding) {
            etPhone.doAfterTextChanged { text ->
                viewModel.updatePhoneNumber(text?.toString() ?: "")
            }

            etCode.doAfterTextChanged { text ->
                viewModel.updateVerificationCode(text?.toString() ?: "")
            }
        }
    }

    private fun setupClickListeners() {
        with(binding) {
            btnRequestCode.setOnSingleClickListener {
                viewModel.requestVerificationCode()
            }

            btnResendCode.setOnSingleClickListener {
                viewModel.resendVerificationCode()
            }

            btnVerify.setOnSingleClickListener {
                viewModel.verifyCode()
            }
        }
    }

    private fun showCodeInputSection() {
        with(binding) {
            tvCodeLabel.visible()
            layoutCode.visible()
            tvTimer.visible()
            tvInfo.visible()

            // 인증 요청 버튼 텍스트 변경
            btnRequestCode.text = "재요청"
        }
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, IDVerifyActivity::class.java)
        }
    }
}
