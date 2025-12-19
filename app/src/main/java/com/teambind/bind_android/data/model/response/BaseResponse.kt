package com.teambind.bind_android.data.model.response

import com.google.gson.annotations.SerializedName

data class BaseResponse<T>(
    @SerializedName("success")
    val isSuccess: Boolean,
    @SerializedName("code")
    val responseCode: Int?,
    @SerializedName("request")
    val request: ResponseRequest?,
    @SerializedName("data")
    val result: T?
)

data class ResponseRequest(
    @SerializedName("path")
    val path: String?,
    @SerializedName("url")
    val url: String?
)

class EmptyResult
