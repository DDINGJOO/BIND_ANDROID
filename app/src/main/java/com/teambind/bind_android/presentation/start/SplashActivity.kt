package com.teambind.bind_android.presentation.start

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.teambind.bind_android.data.local.PreferencesManager
import com.teambind.bind_android.data.local.TokenManager
import com.teambind.bind_android.data.repository.AuthRepository
import com.teambind.bind_android.data.repository.ProfileRepository
import com.teambind.bind_android.databinding.ActivitySplashBinding
import com.teambind.bind_android.presentation.main.MainActivity
import com.teambind.bind_android.presentation.start.auth.AuthMainActivity
import com.teambind.bind_android.presentation.start.profilesetting.ProfileSettingActivity
import com.teambind.bind_android.util.extension.startActivityAndFinish
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    @Inject
    lateinit var preferencesManager: PreferencesManager

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var profileRepository: ProfileRepository

    private lateinit var binding: ActivitySplashBinding
    private var keepSplashScreen = true

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        splashScreen.setKeepOnScreenCondition { keepSplashScreen }

        lifecycleScope.launch {
            // 시스템 스플래시 즉시 종료
            keepSplashScreen = false

            // 1초 후 애니메이션 시작
            delay(1000)
            startLogoAnimation()
        }
    }

    private fun startLogoAnimation() {
        val logo = binding.ivLogo

        // 로그인 화면의 로고 위치로 이동 (상단에서 80dp + 로고 높이 절반 위치)
        // 현재 중앙에 있으므로, 화면 중앙에서 상단 80dp 위치까지의 거리 계산
        val screenHeight = resources.displayMetrics.heightPixels
        val density = resources.displayMetrics.density
        val targetTopMargin = 80 * density // 80dp
        val logoHeight = 42 * density // 스플래시 로고 높이 42dp (로그인 화면과 동일)

        // 중앙에서 목표 위치까지의 거리
        val centerY = screenHeight / 2f
        val targetY = targetTopMargin + (logoHeight / 2f)
        val translationDistance = targetY - centerY

        val animator = ObjectAnimator.ofFloat(logo, View.TRANSLATION_Y, 0f, translationDistance)
        animator.duration = 600
        animator.interpolator = AccelerateDecelerateInterpolator()

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                lifecycleScope.launch {
                    delay(200)
                    checkAndAutoLogin()
                }
            }
        })

        animator.start()
    }

    private suspend fun checkAndAutoLogin() {
        val hasToken = tokenManager.hasValidToken()
        val hasRefreshToken = !tokenManager.refreshToken.isNullOrEmpty()

        when {
            hasToken && hasRefreshToken -> {
                // 토큰이 있으면 자동 로그인 시도
                tryAutoLogin()
            }
            else -> {
                // 토큰이 없으면 인증 화면으로
                navigateToAuth()
            }
        }
    }

    private suspend fun tryAutoLogin() {
        // 토큰 갱신 시도
        authRepository.refreshToken()
            .onSuccess { response ->
                // 토큰 갱신 성공 - 새 토큰 저장
                tokenManager.saveAllTokens(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken,
                    deviceId = response.deviceId
                )

                // 프로필 조회하여 userId 업데이트
                fetchProfileAndNavigate()
            }
            .onFailure {
                // 토큰 갱신 실패 - 로그아웃 처리 후 인증 화면으로
                tokenManager.clearAllTokens()
                preferencesManager.clearAll()
                navigateToAuth()
            }
    }

    private suspend fun fetchProfileAndNavigate() {
        profileRepository.getMyProfile()
            .onSuccess { profileResponse ->
                // 프로필 정보 저장
                val userId = profileResponse.profile.userId.toLongOrNull() ?: 0L
                preferencesManager.saveUserInfo(
                    userId = userId,
                    userName = profileResponse.profile.nickname,
                    profileImageUrl = profileResponse.profile.profileImageUrl
                )
                preferencesManager.setHasProfile(true)

                // 메인 화면으로 이동
                navigateToMain()
            }
            .onFailure {
                // 프로필이 없으면 프로필 설정으로
                preferencesManager.setIsLoggedIn(true)
                preferencesManager.setHasProfile(false)
                navigateToProfileSetting()
            }
    }

    private fun navigateToMain() {
        startActivityAndFinish<MainActivity>()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun navigateToProfileSetting() {
        startActivityAndFinish<ProfileSettingActivity>()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun navigateToAuth() {
        startActivityAndFinish<AuthMainActivity>()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
