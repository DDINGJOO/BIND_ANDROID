package com.teambind.bind_android.presentation.settings

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import com.teambind.bind_android.databinding.ActivityChangePasswordBinding
import com.teambind.bind_android.presentation.base.BaseActivity
import com.teambind.bind_android.util.extension.collectLatestFlow
import com.teambind.bind_android.util.extension.gone
import com.teambind.bind_android.util.extension.setOnSingleClickListener
import com.teambind.bind_android.util.extension.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangePasswordActivity : BaseActivity<ActivityChangePasswordBinding>() {

    private val viewModel: ChangePasswordViewModel by viewModels()

    override fun inflateBinding(): ActivityChangePasswordBinding {
        return ActivityChangePasswordBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        setupToolbar()
        setupInputListeners()
        setupClickListeners()
    }

    override fun initObserver() {
        collectLatestFlow(viewModel.state) { state ->
            when (state) {
                is ChangePasswordState.Idle -> {
                    binding.loadingOverlay.gone()
                }
                is ChangePasswordState.Loading -> {
                    binding.loadingOverlay.visible()
                }
                is ChangePasswordState.Success -> {
                    binding.loadingOverlay.gone()
                    showToast("비밀번호가 변경되었습니다.")
                    finish()
                }
                is ChangePasswordState.Error -> {
                    binding.loadingOverlay.gone()
                    showToast(state.message)
                }
            }
        }

        collectLatestFlow(viewModel.isButtonEnabled) { isEnabled ->
            binding.btnConfirm.isEnabled = isEnabled
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupInputListeners() {
        binding.etNewPassword.doAfterTextChanged { text ->
            viewModel.updateNewPassword(text?.toString() ?: "")
        }

        binding.etConfirmPassword.doAfterTextChanged { text ->
            viewModel.updateConfirmPassword(text?.toString() ?: "")
        }
    }

    private fun setupClickListeners() {
        binding.btnConfirm.setOnSingleClickListener {
            viewModel.changePassword()
        }
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, ChangePasswordActivity::class.java)
        }
    }
}
