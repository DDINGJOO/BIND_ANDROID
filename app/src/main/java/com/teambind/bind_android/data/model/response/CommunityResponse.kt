package com.teambind.bind_android.data.model.response

import com.google.gson.annotations.SerializedName

// 커뮤니티 게시글 목록 응답
data class CommunityArticleListResponse(
    @SerializedName("page")
    val page: PagedArticleList
)

// 내 피드 (작성글/댓글단글/좋아요한글) 응답 - activities/feed/me/{category}
data class MyFeedResponse(
    @SerializedName("articles")
    val articles: List<ArticleDto>,
    @SerializedName("nextCursor")
    val nextCursor: String?
) {
    // CommunityArticleListResponse와 호환되도록 변환
    fun toPagedArticleList(): PagedArticleList {
        return PagedArticleList(
            articleList = articles,
            nextCursorUpdatedAt = null,
            nextCursorId = nextCursor,
            hasNext = nextCursor != null,
            size = articles.size
        )
    }
}

data class PagedArticleList(
    @SerializedName("items")
    val articleList: List<ArticleDto>,
    @SerializedName("nextCursorUpdatedAt")
    val nextCursorUpdatedAt: String?,
    @SerializedName("nextCursorId")
    val nextCursorId: String?,
    @SerializedName("hasNext")
    val hasNext: Boolean,
    @SerializedName("size")
    val size: Int
)

data class ArticleDto(
    @SerializedName("articleId")
    val articleId: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("writerId")
    val writerId: String,
    @SerializedName("board")
    val board: BoardDto,
    @SerializedName("status")
    val status: String,
    @SerializedName("viewCount")
    val viewCount: Int,
    @SerializedName("firstImageUrl")
    val thumbnailImageUrl: String?,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("images")
    val images: List<ImageDto>,
    @SerializedName("keywords")
    val keywords: List<KeywordDto>,
    @SerializedName("commentCount")
    val commentCount: Int,
    @SerializedName("likeCount")
    val likeCount: Int,
    @SerializedName("writerName")
    val nickname: String,
    @SerializedName("writerProfileImage")
    val profileImageUrl: String?
)

data class BoardDto(
    @SerializedName("boardId")
    val id: Long,
    @SerializedName("boardName")
    val name: String,
    @SerializedName("description")
    val description: String
)

data class KeywordDto(
    @SerializedName("keywordId")
    val keywordId: Long,
    @SerializedName("keywordName")
    val keywordName: String,
    @SerializedName("isCommon")
    val isCommon: Boolean,
    @SerializedName("boardId")
    val boardId: Long?,
    @SerializedName("boardName")
    val boardName: String?
)

data class ImageDto(
    @SerializedName("imageUrl")
    val imageUrl: String,
    @SerializedName("imageId")
    val imageId: String,
    @SerializedName("sequence")
    val sequence: Int
)

// 게시글 상세 응답
data class CommunityDetailResponse(
    @SerializedName("article")
    val article: ArticleDetailDto,
    @SerializedName("comments")
    val comments: List<CommentDto>,
    @SerializedName("likeDetail")
    val likeDetail: LikeDetailDto?
) {
    val isLiked: Boolean
        get() = likeDetail?.isOwn ?: false
}

data class LikeDetailDto(
    @SerializedName("likeCount")
    val likeCount: Int,
    @SerializedName("isOwn")
    val isOwn: Boolean
)

data class ArticleDetailDto(
    @SerializedName("articleId")
    val articleId: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("writerId")
    val writerId: String,
    @SerializedName("board")
    val board: BoardDto,
    @SerializedName("status")
    val status: String,
    @SerializedName("viewCount")
    val viewCount: Int,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("images")
    val images: List<ImageDto>,
    @SerializedName("keywords")
    val keywords: List<KeywordDto>,
    @SerializedName("commentCount")
    val commentCount: Int,
    @SerializedName("likeCount")
    val likeCount: Int,
    @SerializedName("writerName")
    val nickname: String,
    @SerializedName("writerProfileImage")
    val profileImageUrl: String?,
    @SerializedName("eventStartDate")
    val eventStartDate: String?,
    @SerializedName("eventEndDate")
    val eventEndDate: String?
)

data class CommentDto(
    @SerializedName("commentId")
    val commentId: String,
    @SerializedName("writerId")
    val writerId: String,
    @SerializedName("parentCommentId")
    val parentCommentId: String?,
    @SerializedName("rootCommentId")
    val rootCommentId: String?,
    @SerializedName("depth")
    val depth: Int = 0,
    @SerializedName("contents")
    val content: String,
    @SerializedName("replyCount")
    val replyCount: Int = 0,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("replies")
    val replies: List<CommentDto>?,
    @SerializedName("isEdited")
    val isEdited: Boolean = false,
    @SerializedName("visible")
    val isVisible: Boolean = true,
    @SerializedName("isOwn")
    val isOwn: Boolean = false,
    @SerializedName("nickname")
    val writerName: String,
    @SerializedName("profileImageUrl")
    val writerProfileImage: String?
)

// 댓글 생성 응답
data class CreateCommentResponse(
    @SerializedName("commentId")
    val commentId: String
)

// 공지사항/이벤트 목록 응답 (Spring Page 형식)
data class NoticeEventListResponse(
    @SerializedName("content")
    val content: List<ArticleDto>?,
    @SerializedName("totalElements")
    val totalElements: Int?,
    @SerializedName("totalPages")
    val totalPages: Int?,
    @SerializedName("last")
    val last: Boolean?,
    @SerializedName("first")
    val first: Boolean?,
    @SerializedName("empty")
    val empty: Boolean?
)

// 게시글 작성 응답
data class ArticlePostResponse(
    @SerializedName("articleId")
    val articleId: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("board")
    val board: BoardDto,
    @SerializedName("createdAt")
    val createdAt: String
)
