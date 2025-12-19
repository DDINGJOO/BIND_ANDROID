package com.teambind.bind_android.data.model.request

import com.google.gson.annotations.SerializedName

data class EditProfileRequest(
    @SerializedName("nickname")
    val nickname: String? = null,
    @SerializedName("sex")
    val sex: String?,
    @SerializedName("city")
    val city: String?,
    @SerializedName("chattable")
    val isChattable: Boolean,
    @SerializedName("publicProfile")
    val isPublic: Boolean,
    @SerializedName("profileImageId")
    val profileImageId: String?,
    @SerializedName("introduction")
    val introduction: String?,
    @SerializedName("genres")
    val genres: List<Int>,
    @SerializedName("instruments")
    val instruments: List<Int>
)
