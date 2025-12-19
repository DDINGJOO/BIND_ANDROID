package com.teambind.bind_android.data.model.request

import com.google.gson.annotations.SerializedName

// 게시글 작성 요청
data class CreateArticleRequest(
    @SerializedName("title")
    val title: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("boardIds")
    val boardIds: Long,
    @SerializedName("imageIds")
    val imageIds: List<String> = emptyList(),
    @SerializedName("keywordIds")
    val keywordIds: List<Long> = emptyList(),
    @SerializedName("eventStartDate")
    val eventStartDate: String? = null,
    @SerializedName("eventEndDate")
    val eventEndDate: String? = null
)

// 댓글 작성 요청
data class CreateCommentRequest(
    @SerializedName("articleId")
    val articleId: String,
    @SerializedName("contents")
    val contents: String
)

// 좋아요 요청
data class LikeRequest(
    @SerializedName("articleId")
    val articleId: String
)
