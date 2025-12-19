package com.teambind.bind_android.presentation.start.auth

import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.teambind.bind_android.R
import com.teambind.bind_android.databinding.ActivityAuthMainBinding
import com.teambind.bind_android.presentation.base.BaseActivity
import com.teambind.bind_android.presentation.main.MainActivity
import com.teambind.bind_android.presentation.start.emailauth.EmailAuthActivity
import com.teambind.bind_android.presentation.start.findpassword.FindPasswordActivity
import com.teambind.bind_android.util.extension.setOnSingleClickListener
import com.teambind.bind_android.util.extension.startActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AuthMainActivity : BaseActivity<ActivityAuthMainBinding>() {

    private val viewModel: AuthMainViewModel by viewModels()

    override fun inflateBinding(): ActivityAuthMainBinding {
        return ActivityAuthMainBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        setupClickListeners()
        setupTextWatchers()
        observeViewModel()
    }

    private fun setupClickListeners() {
        with(binding) {
            // 로그인 버튼
            btnLogin.setOnSingleClickListener {
                val email = etEmail.text.toString()
                val password = etPassword.text.toString()

                if (validateLogin(email, password)) {
                    viewModel.login(email, password)
                }
            }

            // 비밀번호 찾기
            tvFindPassword.setOnSingleClickListener {
                startActivity<FindPasswordActivity>()
            }

            // 회원가입
            btnSignUp.setOnSingleClickListener {
                startActivity<EmailAuthActivity>()
            }

            // 카카오 로그인
            btnKakaoLogin.setOnSingleClickListener {
                viewModel.kakaoLogin()
            }

            // 구글 로그인
            btnGoogleLogin.setOnSingleClickListener {
                showToast("구글 로그인은 준비 중입니다.")
            }

            // 애플 로그인
            btnAppleLogin.setOnSingleClickListener {
                showToast("Apple 로그인은 준비 중입니다.")
            }
        }
    }

    private fun setupTextWatchers() {
        with(binding) {
            etEmail.addTextChangedListener {
                ivEmailError.visibility = View.GONE
                hideAlertMessage()
            }

            etPassword.addTextChangedListener {
                ivPasswordError.visibility = View.GONE
                hideAlertMessage()
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.loginState.collect { state ->
                when (state) {
                    is AuthMainViewModel.LoginState.Loading -> {
                        // 로딩 표시
                        binding.btnLogin.isEnabled = false
                    }

                    is AuthMainViewModel.LoginState.Success -> {
                        binding.btnLogin.isEnabled = true
                        // 메인 화면으로 이동
                        startActivity<MainActivity>()
                        finishAffinity()
                    }

                    is AuthMainViewModel.LoginState.Error -> {
                        binding.btnLogin.isEnabled = true
                        showAlertMessage(state.message)
                    }

                    is AuthMainViewModel.LoginState.Idle -> {
                        binding.btnLogin.isEnabled = true
                    }
                }
            }
        }
    }

    private fun validateLogin(email: String, password: String): Boolean {
        with(binding) {
            // 이메일 빈 입력값
            if (email.isEmpty()) {
                ivEmailError.visibility = View.VISIBLE
                showAlertMessage(getString(R.string.error_email_empty))
                return false
            }

            // 이메일 정규식 검사
            if (!isValidEmail(email)) {
                ivEmailError.visibility = View.VISIBLE
                showAlertMessage(getString(R.string.error_email_invalid))
                return false
            }

            // 이메일 검사 통과
            ivEmailError.visibility = View.GONE

            // 비밀번호 빈 입력값
            if (password.isEmpty()) {
                ivPasswordError.visibility = View.VISIBLE
                showAlertMessage(getString(R.string.error_password_empty))
                return false
            }

            // 비밀번호 정규식 검사
            if (!isValidPassword(password)) {
                ivPasswordError.visibility = View.VISIBLE
                showAlertMessage(getString(R.string.error_password_invalid))
                return false
            }

            // 비밀번호 검사 통과
            ivPasswordError.visibility = View.GONE
            hideAlertMessage()

            return true
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"
        return email.matches(emailRegex.toRegex())
    }

    private fun isValidPassword(password: String): Boolean {
        // 8자 이상, 영문+숫자+특수문자 포함
        return password.length >= 8
    }

    private fun showAlertMessage(message: String) {
        with(binding) {
            layoutAlertMessage.visibility = View.VISIBLE
            tvAlertMessage.text = message
        }
    }

    private fun hideAlertMessage() {
        binding.layoutAlertMessage.visibility = View.GONE
    }
}
