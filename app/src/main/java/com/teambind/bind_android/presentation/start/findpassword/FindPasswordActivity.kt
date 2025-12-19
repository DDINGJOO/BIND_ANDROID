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
                if (email.isNotEmpty()) {
                    // TODO: 비밀번호 찾기 API 호출
                    showToast("비밀번호 재설정 이메일을 발송했습니다.")
                    finish()
                } else {
                    showToast("이메일을 입력해주세요.")
                }
            }
        }
    }
}
