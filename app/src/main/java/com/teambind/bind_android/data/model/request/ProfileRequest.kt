package com.teambind.bind_android.data.model.request

import com.google.gson.annotations.SerializedName

// 프로필 생성 요청 (회원가입 시)
data class CreateProfileRequest(
    @SerializedName("nickname")
    val nickname: String,
    @SerializedName("sex")
    val sex: String?,
    @SerializedName("profileImageUrl")
    val profileImageUrl: String?,
    @SerializedName("genres")
    val genres: List<String>,
    @SerializedName("instruments")
    val instruments: List<String>,
    @SerializedName("city")
    val city: String?,
    @SerializedName("introduction")
    val introduction: String?
)
