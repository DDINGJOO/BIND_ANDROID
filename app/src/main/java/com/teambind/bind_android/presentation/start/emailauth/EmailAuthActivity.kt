package com.teambind.bind_android.presentation.start.emailauth

import android.content.Intent
import android.os.CountDownTimer
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import com.teambind.bind_android.R
import com.teambind.bind_android.databinding.ActivityEmailAuthBinding
import com.teambind.bind_android.presentation.base.BaseActivity
import com.teambind.bind_android.presentation.start.signup.SignUpActivity
import com.teambind.bind_android.util.extension.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EmailAuthActivity : BaseActivity<ActivityEmailAuthBinding>() {

    private val viewModel: EmailAuthViewModel by viewModels()
    private var countDownTimer: CountDownTimer? = null
    private var shouldShowAlert = true

    override fun inflateBinding(): ActivityEmailAuthBinding {
        return ActivityEmailAuthBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        setupToolbar()
        setupInputListeners()
        setupClickListeners()
    }

    override fun initObserver() {
        collectLatestFlow(viewModel.emailAuthState) { state ->
            when (state) {
                is EmailAuthState.Idle -> {
                    // 초기 상태
                }

                is EmailAuthState.CodeSent -> {
                    // 인증 코드 발송됨
                    with(binding) {
                        btnRequestCode.text = "다시받기"
                        etEmail.isEnabled = false
                        etEmail.setTextColor(getColor(R.color.gray4))
                        layoutAuthCode.visible()
                        tvGuideMessage.visible()
                    }
                    startTimer()
                }

                is EmailAuthState.CodeVerified -> {
                    // 인증 성공
                    shouldShowAlert = false
                    stopTimer()
                    navigateToSignUp()
                }

                is EmailAuthState.Error -> {
                    showAlertMessage(state.message)
                }

                is EmailAuthState.Loading -> {
                    // 로딩 중
                }
            }
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupInputListeners() {
        with(binding) {
            etEmail.doAfterTextChanged {
                ivEmailError.gone()
                layoutAlertMessage.gone()
            }

            etAuthCode.doAfterTextChanged { text ->
                val isValid = text?.length == 6 && text.toString().all { it.isDigit() }
                btnNext.isEnabled = isValid
            }
        }
    }

    private fun setupClickListeners() {
        with(binding) {
            btnRequestCode.setOnSingleClickListener {
                if (validateEmail()) {
                    viewModel.requestEmailCode(etEmail.text.toString())
                }
            }

            btnNext.setOnSingleClickListener {
                val code = etAuthCode.text.toString()
                if (code.length == 6) {
                    viewModel.verifyEmailCode(code)
                }
            }
        }
    }

    private fun validateEmail(): Boolean {
        val email = binding.etEmail.text.toString()

        if (email.isEmpty()) {
            binding.ivEmailError.visible()
            showAlertMessage("이메일 주소를 입력해주세요.")
            return false
        }

        if (!email.isValidEmail()) {
            binding.ivEmailError.visible()
            showAlertMessage("이메일 형식을 확인해주세요.")
            return false
        }

        binding.ivEmailError.gone()
        binding.layoutAlertMessage.gone()
        return true
    }

    private fun showAlertMessage(message: String) {
        with(binding) {
            tvAlertMessage.text = message
            layoutAlertMessage.visible()
            layoutAlertMessage.postDelayed({
                layoutAlertMessage.gone()
            }, 2000)
        }
    }

    private fun startTimer() {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(300000, 1000) { // 5분
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 60000
                val seconds = (millisUntilFinished % 60000) / 1000
                binding.tvTimer.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                binding.tvTimer.text = "00:00"
                if (shouldShowAlert) {
                    showAlertMessage("인증번호 입력 시간이 초과되었습니다.")
                    resetEmailAuth()
                }
            }
        }.start()
    }

    private fun stopTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
    }

    private fun resetEmailAuth() {
        with(binding) {
            etEmail.isEnabled = true
            etEmail.setTextColor(getColor(R.color.black))
            layoutAuthCode.gone()
            tvGuideMessage.gone()
            etAuthCode.text?.clear()
            btnRequestCode.text = "인증받기"
            btnNext.isEnabled = false
        }
    }

    private fun navigateToSignUp() {
        val intent = Intent(this, SignUpActivity::class.java).apply {
            putExtra(SignUpActivity.EXTRA_EMAIL, viewModel.email)
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
    }
}
