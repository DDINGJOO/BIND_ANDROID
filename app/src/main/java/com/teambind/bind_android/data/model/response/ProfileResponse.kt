package com.teambind.bind_android.data.model.response

import com.google.gson.annotations.SerializedName

// 프로필 응답
data class ProfileResponse(
    @SerializedName("profile")
    val profile: ProfileDto,
    @SerializedName("liked")
    val likedList: List<String>
)

data class ProfileDto(
    @SerializedName("userId")
    val userId: String,
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
    @SerializedName("nickname")
    val nickname: String,
    @SerializedName("isChattable")
    val isChattable: Boolean,
    @SerializedName("isPublic")
    val isPublic: Boolean,
    @SerializedName("introduction")
    val introduction: String?
)

// 이미지 업로드 응답
data class ImageUploadResponse(
    @SerializedName("imageUrl")
    val imageUrl: String,
    @SerializedName("id")
    val imageId: String,
    @SerializedName("status")
    val status: String
)
