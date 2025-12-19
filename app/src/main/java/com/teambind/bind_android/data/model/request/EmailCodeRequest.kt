package com.teambind.bind_android.data.model.request

import com.google.gson.annotations.SerializedName

data class EmailCodeRequest(
    @SerializedName("email")
    val email: String
)
