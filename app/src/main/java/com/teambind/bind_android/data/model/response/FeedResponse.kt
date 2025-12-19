package com.teambind.bind_android.data.model.response

import com.google.gson.annotations.SerializedName

// 미니피드 응답
data class FeedResponse(
    @SerializedName("articles")
    val articles: List<FeedArticleDto>,
    @SerializedName("nextCursor")
    val nextCursor: String?
)

data class FeedArticleDto(
    @SerializedName("articleId")
    val articleId: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("board")
    val board: BoardDto,
    @SerializedName("writerId")
    val writerId: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("writerName")
    val writerName: String,
    @SerializedName("writerProfileImage")
    val writerProfileImage: String?,
    @SerializedName("commentCount")
    val commentCount: Int,
    @SerializedName("likeCount")
    val likeCount: Int,
    @SerializedName("firstImageUrl")
    val firstImageUrl: String?
)
