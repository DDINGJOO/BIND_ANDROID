package com.teambind.bind_android.presentation.settings

import android.content.Context
import android.content.Intent
import androidx.activity.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.teambind.bind_android.databinding.ActivitySettingsBinding
import com.teambind.bind_android.presentation.base.BaseActivity
import com.teambind.bind_android.presentation.start.auth.AuthMainActivity
import com.teambind.bind_android.presentation.start.profilesetting.ProfileSettingActivity
import com.teambind.bind_android.util.extension.collectLatestFlow
import com.teambind.bind_android.util.extension.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : BaseActivity<ActivitySettingsBinding>() {

    private val viewModel: SettingsViewModel by viewModels()

    override fun inflateBinding(): ActivitySettingsBinding {
        return ActivitySettingsBinding.inflate(layoutInflater)
    }

    override fun initView() {
        setupToolbar()
        setupMenuItems()
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnSingleClickListener {
            finish()
        }
    }

    private fun setupMenuItems() {
        // 새 레이아웃은 직접 텍스트가 지정되어 있으므로 별도 설정 불필요
    }

    private fun setupClickListeners() {
        with(binding) {
            // Edit Profile
            menuEditProfile.setOnSingleClickListener {
                startActivity(ProfileSettingActivity.newIntent(this@SettingsActivity, isEditMode = true))
            }

            // Change Password
            menuChangePassword.setOnSingleClickListener {
                showToast("비밀번호 변경 화면으로 이동")
            }

            // Push notification toggle
            switchPush.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setPushNotification(isChecked)
            }

            // Marketing notification toggle
            switchMarketing.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setMarketingNotification(isChecked)
            }

            // Logout
            menuLogout.setOnSingleClickListener {
                showLogoutConfirmDialog()
            }

            // Delete Account
            menuDeleteAccount.setOnSingleClickListener {
                showDeleteAccountConfirmDialog()
            }
        }
    }

    private fun showLogoutConfirmDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("로그아웃")
            .setMessage("정말 로그아웃 하시겠습니까?")
            .setPositiveButton("로그아웃") { _, _ ->
                viewModel.logout()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showDeleteAccountConfirmDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("회원 탈퇴")
            .setMessage("정말 탈퇴하시겠습니까?\n탈퇴 시 모든 데이터가 삭제되며 복구할 수 없습니다.")
            .setPositiveButton("탈퇴") { _, _ ->
                viewModel.deleteAccount()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    override fun initObserver() {
        collectLatestFlow(viewModel.uiState) { state ->
            // Update switch states
            binding.switchPush.isChecked = state.isPushEnabled
            binding.switchMarketing.isChecked = state.isMarketingEnabled

            // Error
            state.errorMessage?.let {
                showToast(it)
                viewModel.clearError()
            }
        }

        collectLatestFlow(viewModel.logoutState) { isLoggedOut ->
            if (isLoggedOut) {
                navigateToLogin()
            }
        }

        collectLatestFlow(viewModel.deleteAccountState) { isDeleted ->
            if (isDeleted) {
                showToast("회원 탈퇴가 완료되었습니다")
                navigateToLogin()
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, AuthMainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, SettingsActivity::class.java)
            context.startActivity(intent)
        }
    }
}
