package com.teambind.bind_android.presentation.start.auth

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import com.teambind.bind_android.databinding.ActivityAuthenticationBinding
import com.teambind.bind_android.presentation.base.BaseActivity
import com.teambind.bind_android.util.extension.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthenticationActivity : BaseActivity<ActivityAuthenticationBinding>() {

    override fun inflateBinding(): ActivityAuthenticationBinding {
        return ActivityAuthenticationBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        setupToolbar()
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        binding.btnVerify.setOnSingleClickListener {
            startActivity(IDVerifyActivity.createIntent(this))
        }
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, AuthenticationActivity::class.java)
        }
    }
}
