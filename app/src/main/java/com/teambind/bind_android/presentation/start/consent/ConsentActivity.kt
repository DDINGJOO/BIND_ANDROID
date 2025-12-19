package com.teambind.bind_android.presentation.start.consent

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import androidx.activity.viewModels
import com.teambind.bind_android.R
import com.teambind.bind_android.databinding.ActivityConsentBinding
import com.teambind.bind_android.presentation.base.BaseActivity
import com.teambind.bind_android.util.extension.collectLatestFlow
import com.teambind.bind_android.util.extension.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ConsentActivity : BaseActivity<ActivityConsentBinding>() {

    private val viewModel: ConsentViewModel by viewModels()

    override fun inflateBinding(): ActivityConsentBinding {
        return ActivityConsentBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        val email = intent.getStringExtra(EXTRA_EMAIL) ?: ""
        val password = intent.getStringExtra(EXTRA_PASSWORD) ?: ""
        viewModel.setCredentials(email, password)

        setupClickListeners()
    }

    override fun initObserver() {
        collectLatestFlow(viewModel.consentState) { state ->
            with(binding) {
                // All agree checkbox
                ivAllAgreeCheck.setImageResource(
                    if (state.isAllAgreed) R.drawable.ic_checkbox_checked
                    else R.drawable.ic_checkbox_unchecked
                )

                // Individual checkboxes
                ivServiceCheck.setImageResource(
                    if (state.serviceConsent) R.drawable.ic_checkbox_small_checked
                    else R.drawable.ic_checkbox_small_unchecked
                )
                ivPrivacyCheck.setImageResource(
                    if (state.privacyConsent) R.drawable.ic_checkbox_small_checked
                    else R.drawable.ic_checkbox_small_unchecked
                )
                ivMarketingCheck.setImageResource(
                    if (state.marketingConsent) R.drawable.ic_checkbox_small_checked
                    else R.drawable.ic_checkbox_small_unchecked
                )

                // Sign up button enabled state
                btnSignUp.isEnabled = state.isRequiredAgreed
            }
        }

        collectLatestFlow(viewModel.signUpResult) { result ->
            result?.let {
                it.onSuccess {
                    showToast("회원가입이 완료되었습니다.")
                    // Navigate to profile setting or login
                    finishAffinity()
                    // TODO: Navigate to ProfileSettingActivity
                }
                it.onFailure { exception ->
                    showToast(exception.message ?: "회원가입에 실패했습니다.")
                }
            }
        }
    }

    private fun setupClickListeners() {
        with(binding) {
            // Close button
            btnClose.setOnSingleClickListener {
                finish()
            }

            // All agree
            layoutAllAgree.setOnSingleClickListener {
                viewModel.toggleAllAgree()
            }

            // Individual consents
            layoutServiceConsent.setOnSingleClickListener {
                viewModel.toggleServiceConsent()
            }
            layoutPrivacyConsent.setOnSingleClickListener {
                viewModel.togglePrivacyConsent()
            }
            layoutMarketingConsent.setOnSingleClickListener {
                viewModel.toggleMarketingConsent()
            }

            // Detail buttons
            btnServiceDetail.setOnSingleClickListener {
                openUrl(SERVICE_TERMS_URL)
            }
            btnPrivacyDetail.setOnSingleClickListener {
                openUrl(PRIVACY_POLICY_URL)
            }
            btnMarketingDetail.setOnSingleClickListener {
                openUrl(MARKETING_TERMS_URL)
            }

            // Sign up button
            btnSignUp.setOnSingleClickListener {
                viewModel.signUp()
            }
        }
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            showToast("링크를 열 수 없습니다.")
        }
    }

    companion object {
        const val EXTRA_EMAIL = "extra_email"
        const val EXTRA_PASSWORD = "extra_password"

        // TODO: Replace with actual URLs
        private const val SERVICE_TERMS_URL = "https://bind.app/terms"
        private const val PRIVACY_POLICY_URL = "https://bind.app/privacy"
        private const val MARKETING_TERMS_URL = "https://bind.app/marketing"
    }
}
