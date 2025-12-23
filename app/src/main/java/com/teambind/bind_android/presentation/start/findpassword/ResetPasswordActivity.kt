package com.teambind.bind_android.presentation.start.findpassword

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import com.teambind.bind_android.R
import com.teambind.bind_android.databinding.ActivityResetPasswordBinding
import com.teambind.bind_android.presentation.base.BaseActivity
import com.teambind.bind_android.presentation.start.auth.AuthMainActivity
import com.teambind.bind_android.util.extension.collectLatestFlow
import com.teambind.bind_android.util.extension.gone
import com.teambind.bind_android.util.extension.setOnSingleClickListener
import com.teambind.bind_android.util.extension.startActivityClearTask
import com.teambind.bind_android.util.extension.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ResetPasswordActivity : BaseActivity<ActivityResetPasswordBinding>() {

    private val viewModel: ResetPasswordViewModel by viewModels()

    override fun inflateBinding(): ActivityResetPasswordBinding {
        return ActivityResetPasswordBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        setupToolbar()
        setupInputListeners()
        setupClickListeners()
        loadInitialEmail()
    }

    private fun loadInitialEmail() {
        val email = intent.getStringExtra(EXTRA_EMAIL) ?: ""
        if (email.isNotEmpty()) {
            binding.etEmail.setText(email)
            viewModel.updateEmail(email)
        }
    }

    override fun initObserver() {
        collectLatestFlow(viewModel.state) { state ->
            when (state) {
                is ResetPasswordState.Idle -> {
                    binding.loadingOverlay.gone()
                }
                is ResetPasswordState.Loading -> {
                    binding.loadingOverlay.visible()
                }
                is ResetPasswordState.Success -> {
                    binding.loadingOverlay.gone()
                    showToast("비밀번호가 변경되었습니다.")
                    startActivityClearTask<AuthMainActivity>()
                }
                is ResetPasswordState.Error -> {
                    binding.loadingOverlay.gone()
                    showToast(state.message)
                }
            }
        }

        collectLatestFlow(viewModel.isButtonEnabled) { isEnabled ->
            binding.btnConfirm.isEnabled = isEnabled
        }

        collectLatestFlow(viewModel.validationState) { validation ->
            updateValidationUI(validation)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupInputListeners() {
        binding.etEmail.doAfterTextChanged { text ->
            viewModel.updateEmail(text?.toString() ?: "")
        }

        binding.etPassword.doAfterTextChanged { text ->
            viewModel.updatePassword(text?.toString() ?: "")
        }

        binding.etPasswordConfirm.doAfterTextChanged { text ->
            viewModel.updatePasswordConfirm(text?.toString() ?: "")
        }
    }

    private fun setupClickListeners() {
        binding.btnConfirm.setOnSingleClickListener {
            viewModel.resetPassword()
        }
    }

    private fun updateValidationUI(state: PasswordValidationState) {
        val checkedIcon = R.drawable.ic_checkbox_small_checked
        val uncheckedIcon = R.drawable.ic_checkbox_small_unchecked

        binding.tvReqUppercase.setCompoundDrawablesWithIntrinsicBounds(
            if (state.hasUppercase) checkedIcon else uncheckedIcon, 0, 0, 0
        )
        binding.tvReqLowercase.setCompoundDrawablesWithIntrinsicBounds(
            if (state.hasLowercase) checkedIcon else uncheckedIcon, 0, 0, 0
        )
        binding.tvReqNumber.setCompoundDrawablesWithIntrinsicBounds(
            if (state.hasNumber) checkedIcon else uncheckedIcon, 0, 0, 0
        )
        binding.tvReqSpecial.setCompoundDrawablesWithIntrinsicBounds(
            if (state.hasSpecialCharacter) checkedIcon else uncheckedIcon, 0, 0, 0
        )
        binding.tvReqLength.setCompoundDrawablesWithIntrinsicBounds(
            if (state.hasValidLength) checkedIcon else uncheckedIcon, 0, 0, 0
        )
        binding.tvReqMatch.setCompoundDrawablesWithIntrinsicBounds(
            if (state.passwordsMatch) checkedIcon else uncheckedIcon, 0, 0, 0
        )
    }

    companion object {
        private const val EXTRA_EMAIL = "extra_email"

        fun createIntent(context: Context, email: String = ""): Intent {
            return Intent(context, ResetPasswordActivity::class.java).apply {
                putExtra(EXTRA_EMAIL, email)
            }
        }
    }
}
