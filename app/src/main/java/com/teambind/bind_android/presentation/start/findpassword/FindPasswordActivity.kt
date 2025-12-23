package com.teambind.bind_android.presentation.start.findpassword

import android.view.LayoutInflater
import com.teambind.bind_android.databinding.ActivityFindPasswordBinding
import com.teambind.bind_android.presentation.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FindPasswordActivity : BaseActivity<ActivityFindPasswordBinding>() {

    override fun inflateBinding(): ActivityFindPasswordBinding {
        return ActivityFindPasswordBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        setupToolbar()
        setupClickListeners()
    }

    private fun setupToolbar() {
        with(binding) {
            toolbar.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun setupClickListeners() {
        with(binding) {
            btnSendEmail.setOnClickListener {
                val email = etEmail.text.toString()
                if (email.isNotEmpty() && isValidEmail(email)) {
                    // 비밀번호 재설정 화면으로 이동
                    startActivity(ResetPasswordActivity.createIntent(this@FindPasswordActivity, email))
                } else {
                    showToast("올바른 이메일을 입력해주세요.")
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }
}
