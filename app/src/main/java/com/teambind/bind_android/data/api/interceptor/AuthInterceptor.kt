package com.teambind.bind_android.data.api.interceptor

import com.teambind.bind_android.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    companion object {
        // 서버 API Gateway 기준: /bff/v1/auth/* 는 인증 불필요 (iOS와 동일)
        private val EXCLUDED_PATHS = listOf(
            "auth/login",           // 로그인, 토큰 갱신
            "auth/signup",          // 회원가입
            "auth/emails",          // 이메일 인증
            "consents"              // 약관
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val url = originalRequest.url.toString()

        // 토큰이 필요하지 않은 경로인지 확인
        val shouldExcludeToken = EXCLUDED_PATHS.any { url.contains(it) }

        val newRequest = if (shouldExcludeToken) {
            originalRequest
        } else {
            val accessToken = tokenManager.accessToken
            if (!accessToken.isNullOrEmpty()) {
                originalRequest.newBuilder()
                    .header("Authorization", "Bearer $accessToken")
                    .build()
            } else {
                originalRequest
            }
        }

        return chain.proceed(newRequest)
    }
}
