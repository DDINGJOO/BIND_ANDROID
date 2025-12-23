package com.teambind.bind_android.util

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.teambind.bind_android.data.repository.AuthRepository
import com.teambind.bind_android.presentation.start.auth.AuthenticationActivity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 본인인증(휴대폰 인증) 상태를 확인하고 필요시 인증 화면으로 이동하는 헬퍼 클래스
 *
 * 사용법:
 * 1. Activity에서 AuthenticationActivity를 시작하기 위한 launcher를 등록
 * 2. checkPhoneVerificationAndProceed()를 호출하여 인증 상태 확인
 * 3. 인증 완료 시 onVerified 콜백 실행, 미인증 시 인증 화면으로 이동
 *
 * Example:
 * ```kotlin
 * private val authLauncher = registerForActivityResult(
 *     ActivityResultContracts.StartActivityForResult()
 * ) { result ->
 *     if (result.resultCode == Activity.RESULT_OK) {
 *         // 인증 완료 후 원래 하려던 작업 수행
 *         proceedWithReservation()
 *     }
 * }
 *
 * // 버튼 클릭 시
 * lifecycleScope.launch {
 *     phoneVerificationHelper.checkPhoneVerificationAndProceed(
 *         activity = this@MyActivity,
 *         authLauncher = authLauncher,
 *         onVerified = { proceedWithReservation() },
 *         onError = { showToast(it) }
 *     )
 * }
 * ```
 */
@Singleton
class PhoneVerificationHelper @Inject constructor(
    private val authRepository: AuthRepository
) {

    /**
     * 휴대폰 인증 상태를 확인하고 인증되지 않은 경우 인증 화면으로 이동
     *
     * @param activity 현재 Activity
     * @param authLauncher AuthenticationActivity를 시작할 ActivityResultLauncher
     * @param onVerified 인증이 완료된 경우 호출되는 콜백
     * @param onError 에러 발생 시 호출되는 콜백 (에러 메시지 전달)
     */
    suspend fun checkPhoneVerificationAndProceed(
        activity: Activity,
        authLauncher: ActivityResultLauncher<Intent>,
        onVerified: () -> Unit,
        onError: ((String) -> Unit)? = null
    ) {
        authRepository.checkPhoneValid()
            .onSuccess { isVerified ->
                if (isVerified) {
                    // 이미 인증된 상태
                    onVerified()
                } else {
                    // 인증 필요 - 인증 화면으로 이동
                    authLauncher.launch(AuthenticationActivity.createIntent(activity))
                }
            }
            .onFailure { error ->
                // API 에러 (401 포함) - 인증 필요
                val message = error.message ?: "휴대폰 인증 확인에 실패했습니다."
                if (message.contains("인증이 필요") || message.contains("401")) {
                    // 인증 필요 에러인 경우 인증 화면으로 이동
                    authLauncher.launch(AuthenticationActivity.createIntent(activity))
                } else {
                    // 다른 에러 (네트워크 오류 등)
                    onError?.invoke(message)
                }
            }
    }

    /**
     * 인증 상태만 확인 (화면 전환 없이)
     *
     * @return 인증 완료 여부 (에러 시 false)
     */
    suspend fun isPhoneVerified(): Boolean {
        return authRepository.checkPhoneValid()
            .getOrDefault(false)
    }
}
