package com.teambind.bind_android.presentation.start.signup

import android.content.Intent
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import com.teambind.bind_android.R
import com.teambind.bind_android.databinding.ActivitySignupBinding
import com.teambind.bind_android.presentation.base.BaseActivity
import com.teambind.bind_android.presentation.start.consent.ConsentActivity
import com.teambind.bind_android.util.extension.collectLatestFlow
import com.teambind.bind_android.util.extension.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpActivity : BaseActivity<ActivitySignupBinding>() {

    private val viewModel: SignUpViewModel by viewModels()

    override fun inflateBinding(): ActivitySignupBinding {
        return ActivitySignupBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        // Get email from EmailAuthActivity
        val email = intent.getStringExtra(EXTRA_EMAIL) ?: ""
        viewModel.setEmail(email)

        setupToolbar()
        setupInputListeners()
        setupClickListeners()
    }

    override fun initObserver() {
        // Password validation state
        collectLatestFlow(viewModel.passwordValidation) { validation ->
            with(binding) {
                ivUppercaseCheck.setImageResource(
                    if (validation.hasUppercase) R.drawable.ic_check_circle else R.drawable.ic_error_circle
                )
                ivLowercaseCheck.setImageResource(
                    if (validation.hasLowercase) R.drawable.ic_check_circle else R.drawable.ic_error_circle
                )
                ivNumberCheck.setImageResource(
                    if (validation.hasNumber) R.drawable.ic_check_circle else R.drawable.ic_error_circle
                )
                ivSpecialCharCheck.setImageResource(
                    if (validation.hasSpecialChar) R.drawable.ic_check_circle else R.drawable.ic_error_circle
                )
                ivLengthCheck.setImageResource(
                    if (validation.hasMinLength) R.drawable.ic_check_circle else R.drawable.ic_error_circle
                )
                ivPasswordConfirmCheck.setImageResource(
                    if (validation.passwordsMatch) R.drawable.ic_check_circle else R.drawable.ic_error_circle
                )

                // Show error icon on password field if not valid
                ivPasswordError.visibility = if (validation.isPasswordValid) {
                    android.view.View.GONE
                } else {
                    android.view.View.VISIBLE
                }

                // Show error icon on password confirm field if not match
                ivPasswordConfirmError.visibility = if (validation.passwordsMatch || viewModel.passwordConfirm.value.isEmpty()) {
                    android.view.View.GONE
                } else {
                    android.view.View.VISIBLE
                }
            }
        }

        // Button enabled state
        collectLatestFlow(viewModel.isButtonEnabled) { isEnabled ->
            binding.btnNext.isEnabled = isEnabled
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupInputListeners() {
        with(binding) {
            etPassword.doAfterTextChanged { text ->
                viewModel.updatePassword(text?.toString() ?: "")
            }

            etPasswordConfirm.doAfterTextChanged { text ->
                viewModel.updatePasswordConfirm(text?.toString() ?: "")
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnNext.setOnSingleClickListener {
            navigateToConsent()
        }
    }

    private fun navigateToConsent() {
        val intent = Intent(this, ConsentActivity::class.java).apply {
            putExtra(ConsentActivity.EXTRA_EMAIL, viewModel.email)
            putExtra(ConsentActivity.EXTRA_PASSWORD, viewModel.password.value)
        }
        startActivity(intent)
    }

    companion object {
        const val EXTRA_EMAIL = "extra_email"
    }
}
