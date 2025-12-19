package com.teambind.bind_android.data.model.request

import com.google.gson.annotations.SerializedName

data class SignUpRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("passwordConfirm")
    val passwordConfirm: String,
    @SerializedName("serviceConsent")
    val serviceConsent: Boolean,
    @SerializedName("privacyConsent")
    val privacyConsent: Boolean,
    @SerializedName("marketingConsent")
    val marketingConsent: Boolean
)
