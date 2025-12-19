package com.teambind.bind_android.data.repository

import com.teambind.bind_android.data.api.service.SignUpService
import com.teambind.bind_android.data.model.request.SignUpRequest
import retrofit2.HttpException
import java.net.UnknownHostException
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignUpRepository @Inject constructor(
    private val signUpService: SignUpService
) {

    // 이메일 인증 코드 발송
    suspend fun requestEmailCode(email: String): Result<Unit> {
        return try {
            val response = signUpService.requestEmailCode(email)

            if (response.isSuccess) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("인증 코드 발송에 실패했습니다. 이메일 주소를 확인해주세요."))
            }
        } catch (e: HttpException) {
            val message = when (e.code()) {
                400 -> "이미 가입된 이메일입니다."
                404 -> "인증 코드 발송에 실패했습니다."
                500, 502, 503 -> "서버에 일시적인 문제가 발생했습니다. 잠시 후 다시 시도해주세요."
                else -> "인증 코드 발송에 실패했습니다. 다시 시도해주세요."
            }
            Result.failure(Exception(message))
        } catch (e: UnknownHostException) {
            Result.failure(Exception("인터넷 연결을 확인해주세요."))
        } catch (e: SocketTimeoutException) {
            Result.failure(Exception("서버 응답이 없습니다. 잠시 후 다시 시도해주세요."))
        } catch (e: Exception) {
            Result.failure(Exception("인증 코드 발송에 실패했습니다. 다시 시도해주세요."))
        }
    }

    // 이메일 인증 코드 확인
    suspend fun verifyEmailCode(email: String, code: String): Result<Boolean> {
        return try {
            val response = signUpService.verifyEmailCode(email, code)

            if (response.isSuccess && response.result == true) {
                Result.success(true)
            } else {
                Result.failure(Exception("인증번호가 일치하지 않습니다."))
            }
        } catch (e: HttpException) {
            val message = when (e.code()) {
                400, 401 -> "인증번호가 일치하지 않습니다."
                410 -> "인증번호가 만료되었습니다. 다시 요청해주세요."
                500, 502, 503 -> "서버에 일시적인 문제가 발생했습니다. 잠시 후 다시 시도해주세요."
                else -> "인증에 실패했습니다. 다시 시도해주세요."
            }
            Result.failure(Exception(message))
        } catch (e: UnknownHostException) {
            Result.failure(Exception("인터넷 연결을 확인해주세요."))
        } catch (e: SocketTimeoutException) {
            Result.failure(Exception("서버 응답이 없습니다. 잠시 후 다시 시도해주세요."))
        } catch (e: Exception) {
            Result.failure(Exception("인증에 실패했습니다. 다시 시도해주세요."))
        }
    }

    // 회원가입
    suspend fun signUp(
        email: String,
        password: String,
        passwordConfirm: String,
        consents: List<Boolean>
    ): Result<Unit> {
        return try {
            val request = SignUpRequest(
                email = email,
                password = password,
                passwordConfirm = passwordConfirm,
                serviceConsent = consents.getOrElse(0) { false },
                privacyConsent = consents.getOrElse(1) { false },
                marketingConsent = consents.getOrElse(2) { false }
            )
            val response = signUpService.signUp(request)

            if (response.isSuccess) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("회원가입에 실패했습니다. 다시 시도해주세요."))
            }
        } catch (e: HttpException) {
            val message = when (e.code()) {
                400 -> "입력 정보를 확인해주세요."
                409 -> "이미 가입된 이메일입니다."
                500, 502, 503 -> "서버에 일시적인 문제가 발생했습니다. 잠시 후 다시 시도해주세요."
                else -> "회원가입에 실패했습니다. 다시 시도해주세요."
            }
            Result.failure(Exception(message))
        } catch (e: UnknownHostException) {
            Result.failure(Exception("인터넷 연결을 확인해주세요."))
        } catch (e: SocketTimeoutException) {
            Result.failure(Exception("서버 응답이 없습니다. 잠시 후 다시 시도해주세요."))
        } catch (e: Exception) {
            Result.failure(Exception("회원가입에 실패했습니다. 다시 시도해주세요."))
        }
    }
}
