package com.teambind.bind_android.data.model.request

import com.google.gson.annotations.SerializedName

data class RefreshTokenRequest(
    @SerializedName("deviceId")
    val deviceId: String,
    @SerializedName("refreshToken")
    val refreshToken: String
)
