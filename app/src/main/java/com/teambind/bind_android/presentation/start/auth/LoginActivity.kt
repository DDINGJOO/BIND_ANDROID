package com.teambind.bind_android.presentation.start.auth

import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import com.teambind.bind_android.databinding.ActivityLoginBinding
import com.teambind.bind_android.presentation.base.BaseActivity
import com.teambind.bind_android.presentation.main.MainActivity
import com.teambind.bind_android.presentation.start.profilesetting.ProfileSettingActivity
import com.teambind.bind_android.util.extension.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : BaseActivity<ActivityLoginBinding>() {

    private val viewModel: LoginViewModel by viewModels()

    override fun inflateBinding(): ActivityLoginBinding {
        return ActivityLoginBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        setupToolbar()
        setupInputListeners()
        setupClickListeners()
    }

    override fun initObserver() {
        collectLatestFlow(viewModel.loginState) { state ->
            when (state) {
                is LoginState.Idle -> {
                    binding.loadingOverlay.gone()
                }

                is LoginState.Loading -> {
                    binding.loadingOverlay.visible()
                }

                is LoginState.Success -> {
                    binding.loadingOverlay.gone()
                    if (state.hasProfile) {
                        startActivityClearTask<MainActivity>()
                    } else {
                        startActivityClearTask<ProfileSettingActivity>()
                    }
                }

                is LoginState.Error -> {
                    binding.loadingOverlay.gone()
                    showToast(state.message)
                }
            }
        }

        collectLatestFlow(viewModel.isButtonEnabled) { isEnabled ->
            binding.btnLogin.isEnabled = isEnabled
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupInputListeners() {
        with(binding) {
            etEmail.doAfterTextChanged { text ->
                viewModel.updateEmail(text?.toString() ?: "")
            }

            etPassword.doAfterTextChanged { text ->
                viewModel.updatePassword(text?.toString() ?: "")
            }
        }
    }

    private fun setupClickListeners() {
        with(binding) {
            btnLogin.setOnSingleClickListener {
                viewModel.login()
            }

            tvFindPassword.setOnSingleClickListener {
                // TODO: 비밀번호 찾기 화면으로 이동
                showToast("비밀번호 찾기는 준비 중입니다.")
            }
        }
    }
}
