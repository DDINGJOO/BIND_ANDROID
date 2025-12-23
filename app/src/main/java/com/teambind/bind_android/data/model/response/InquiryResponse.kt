package com.teambind.bind_android.data.model.response

import com.google.gson.annotations.SerializedName

// 문의 목록 응답
data class InquiryListResponse(
    @SerializedName("inquiries")
    val inquiries: List<InquiryDto>,
    @SerializedName("nextCursor")
    val nextCursor: String?
)

// 문의 상세 응답
data class InquiryDetailResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("contents")
    val contents: String,
    @SerializedName("category")
    val category: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("writerId")
    val writerId: String?,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String?,
    @SerializedName("answeredAt")
    val answeredAt: String?,
    @SerializedName("hasAnswer")
    val hasAnswer: Boolean,
    @SerializedName("answer")
    val answer: String?
) {
    fun getCategoryDisplayName(): String {
        return when (category) {
            "RESERVATION" -> "예약 관련"
            "CHECK_IN" -> "이용/입실"
            "PAYMENT" -> "요금/결제"
            "REVIEW_REPORT" -> "리뷰/신고"
            "ETC" -> "기타"
            else -> category
        }
    }

    fun getStatusDisplayName(): String {
        return when (status) {
            "UNANSWERED" -> "답변 대기"
            "CONFIRMED" -> "확인 중"
            "ANSWERED" -> "답변 완료"
            else -> status
        }
    }

    val isAnswered: Boolean
        get() = status == "ANSWERED" && hasAnswer
}

// 문의 아이템
data class InquiryDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("category")
    val category: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("hasAnswer")
    val hasAnswer: Boolean
) {
    fun getCategoryDisplayName(): String {
        return when (category) {
            "RESERVATION" -> "예약 관련"
            "CHECK_IN" -> "이용/입실"
            "PAYMENT" -> "요금/결제"
            "REVIEW_REPORT" -> "리뷰/신고"
            "ETC" -> "기타"
            else -> category
        }
    }
}
