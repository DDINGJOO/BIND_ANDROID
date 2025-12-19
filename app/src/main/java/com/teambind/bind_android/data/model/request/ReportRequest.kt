package com.teambind.bind_android.data.model.request

import com.google.gson.annotations.SerializedName

data class ReportCreateRequest(
    @SerializedName("reporterId")
    val reporterId: String,
    @SerializedName("reportedId")
    val reportedId: String,
    @SerializedName("referenceType")
    val referenceType: ReferenceType,
    @SerializedName("reportCategory")
    val reportCategory: String,
    @SerializedName("reason")
    val reason: String
)

enum class ReferenceType {
    PROFILE,
    ARTICLE,
    BUSINESS
}

// 게시글(ARTICLE) 신고 카테고리
enum class ArticleReportCategory(val value: String) {
    ABUSE("욕설, 비속어, 음란성 내용을 포함한 게시글"),
    SPAM("도배, 스팸, 광고성 게시글"),
    DISPUTE("분란을 조장하는 게시글"),
    AD("타업체를 광고한 게시글"),
    FRAUD("허위 사기성 내용"),
    ETC("기타")
}

// 프로필(PROFILE) 신고 카테고리
enum class ProfileReportCategory(val value: String) {
    ABUSE("욕설, 비속어, 음란"),
    NICKNAME("부적절한 닉네임"),
    PHOTO("부적절한 프로필 사진"),
    BIO("부적절한 자기소개"),
    ETC("기타")
}
