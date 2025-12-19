package com.teambind.bind_android.data.api.service

import com.teambind.bind_android.data.model.request.CreateArticleRequest
import com.teambind.bind_android.data.model.request.CreateCommentRequest
import com.teambind.bind_android.data.model.response.ArticlePostResponse
import com.teambind.bind_android.data.model.response.BaseResponse
import com.teambind.bind_android.data.model.response.CommunityArticleListResponse
import com.teambind.bind_android.data.model.response.CommunityDetailResponse
import com.teambind.bind_android.data.model.response.CreateCommentResponse
import com.teambind.bind_android.data.model.response.MyFeedResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface CommunityService {

    // 게시글 목록 조회 (서버: GET /bff/v1/communities/articles/regular?boardIds=4)
    @GET("communities/articles/regular")
    suspend fun getArticleList(
        @Query("boardIds") boardIds: Long? = null,
        @Query("size") size: Int = 20,
        @Query("cursorId") cursorId: String? = null,
        @Query("cursorUpdatedAt") cursorUpdatedAt: String? = null,
        @Query("sortBy") sortBy: String = "latest"
    ): BaseResponse<CommunityArticleListResponse>

    // 인기 게시글 조회
    @GET("communities/articles/regular")
    suspend fun getHotArticles(
        @Query("size") size: Int = 10,
        @Query("sortBy") sortBy: String = "latest"
    ): BaseResponse<CommunityArticleListResponse>

    // 게시글 상세 조회 (서버: GET /bff/v1/communities/articles/regular/{articleId})
    @GET("communities/articles/regular/{articleId}")
    suspend fun getArticleDetail(
        @Path("articleId") articleId: String
    ): BaseResponse<CommunityDetailResponse>

    // 게시글 작성 (서버: POST /bff/v1/communities/articles/regular)
    @POST("communities/articles/regular")
    suspend fun createArticle(
        @Body request: CreateArticleRequest
    ): BaseResponse<ArticlePostResponse>

    // 게시글 수정 (서버: PUT /bff/v1/communities/articles/regular/{articleId})
    @PUT("communities/articles/regular/{articleId}")
    suspend fun updateArticle(
        @Path("articleId") articleId: String,
        @Body request: CreateArticleRequest
    ): BaseResponse<ArticlePostResponse>

    // 게시글 삭제
    @DELETE("communities/articles/regular/{articleId}")
    suspend fun deleteArticle(
        @Path("articleId") articleId: String
    ): BaseResponse<Boolean>

    // 댓글 작성 (서버: POST /bff/v1/communities/comments/create)
    @POST("communities/comments/create")
    suspend fun createComment(
        @Body request: CreateCommentRequest,
        @Query("parentId") parentId: String? = null
    ): BaseResponse<CreateCommentResponse>

    // 댓글 삭제
    @DELETE("communities/comments/{commentId}")
    suspend fun deleteComment(
        @Path("commentId") commentId: String
    ): BaseResponse<Boolean>

    // 게시글 좋아요/좋아요 취소 (서버: POST /bff/v1/gaechu/likes/{categoryId}/{referenceId}?likerId=xxx&isLike=true/false)
    // 204 No Content 응답을 처리하기 위해 Response<Unit> 사용
    @POST("gaechu/likes/ARTICLE/{articleId}")
    suspend fun toggleLikeArticle(
        @Path("articleId") articleId: String,
        @Query("likerId") likerId: String,
        @Query("isLike") isLike: Boolean
    ): Response<Unit>

    // 내가 작성한 게시글 목록 조회 (서버: GET /bff/v1/activities/feed/me/article)
    @GET("activities/feed/me/article")
    suspend fun getMyArticles(
        @Query("size") size: Int = 20,
        @Query("cursor") cursor: String? = null
    ): BaseResponse<MyFeedResponse>

    // 내가 댓글 작성한 게시글 목록 조회 (서버: GET /bff/v1/activities/feed/me/comment)
    @GET("activities/feed/me/comment")
    suspend fun getMyCommentedArticles(
        @Query("size") size: Int = 20,
        @Query("cursor") cursor: String? = null
    ): BaseResponse<MyFeedResponse>

    // 내가 좋아요한 게시글 목록 조회 (서버: GET /bff/v1/activities/feed/me/like)
    @GET("activities/feed/me/like")
    suspend fun getMyLikedArticles(
        @Query("size") size: Int = 20,
        @Query("cursor") cursor: String? = null
    ): BaseResponse<MyFeedResponse>
}
