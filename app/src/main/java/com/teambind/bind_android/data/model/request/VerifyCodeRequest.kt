package com.teambind.bind_android.data.model.request

import com.google.gson.annotations.SerializedName

data class VerifyCodeRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("code")
    val code: String
)
