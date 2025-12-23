package com.teambind.bind_android.data.model.request

import com.google.gson.annotations.SerializedName

// SMS 인증 코드 요청
data class SmsCodeRequest(
    @SerializedName("phoneNumber")
    val phoneNumber: String
)

// SMS 인증 코드 확인
data class SmsVerifyRequest(
    @SerializedName("phoneNumber")
    val phoneNumber: String,
    @SerializedName("code")
    val code: String
)
