package com.teambind.bind_android.presentation.settings

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import com.teambind.bind_android.databinding.ActivityAccountSettingBinding
import com.teambind.bind_android.presentation.base.BaseActivity
import com.teambind.bind_android.presentation.start.auth.AuthMainActivity
import com.teambind.bind_android.util.extension.collectLatestFlow
import com.teambind.bind_android.util.extension.gone
import com.teambind.bind_android.util.extension.setOnSingleClickListener
import com.teambind.bind_android.util.extension.startActivityClearTask
import com.teambind.bind_android.util.extension.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccountSettingActivity : BaseActivity<ActivityAccountSettingBinding>() {

    private val viewModel: AccountSettingViewModel by viewModels()

    override fun inflateBinding(): ActivityAccountSettingBinding {
        return ActivityAccountSettingBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        setupToolbar()
        setupClickListeners()
    }

    override fun initObserver() {
        collectLatestFlow(viewModel.state) { state ->
            when (state) {
                is AccountSettingState.Idle -> {
                    binding.loadingOverlay.gone()
                }
                is AccountSettingState.Loading -> {
                    binding.loadingOverlay.visible()
                }
                is AccountSettingState.WithdrawSuccess -> {
                    binding.loadingOverlay.gone()
                    showToast("회원 탈퇴가 완료되었습니다.")
                    startActivityClearTask<AuthMainActivity>()
                }
                is AccountSettingState.Error -> {
                    binding.loadingOverlay.gone()
                    showToast(state.message)
                }
            }
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        binding.btnChangePassword.setOnSingleClickListener {
            startActivity(ChangePasswordActivity.createIntent(this))
        }

        binding.btnWithdraw.setOnSingleClickListener {
            showWithdrawConfirmDialog()
        }
    }

    private fun showWithdrawConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("회원탈퇴")
            .setMessage("정말 탈퇴하시겠습니까?\n탈퇴 시 모든 데이터가 삭제됩니다.")
            .setPositiveButton("회원탈퇴") { _, _ ->
                viewModel.withdraw()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, AccountSettingActivity::class.java)
        }
    }
}
