package com.teambind.bind_android.data.repository

import android.net.Uri
import com.teambind.bind_android.data.api.service.CommunityService
import com.teambind.bind_android.data.local.PreferencesManager
import com.teambind.bind_android.data.model.ImageUploadPurpose
import com.teambind.bind_android.data.model.request.CreateArticleRequest
import com.teambind.bind_android.data.model.request.CreateCommentRequest
import com.teambind.bind_android.data.model.response.ArticlePostResponse
import com.teambind.bind_android.data.model.response.CommunityArticleListResponse
import com.teambind.bind_android.data.model.response.CommunityDetailResponse
import com.teambind.bind_android.data.model.response.CreateCommentResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommunityRepository @Inject constructor(
    private val communityService: CommunityService,
    private val imageRepository: ImageRepository,
    private val preferencesManager: PreferencesManager
) {

    // 게시글 목록 조회
    suspend fun getArticleList(
        boardIds: Long? = null,
        size: Int = 20,
        cursorId: String? = null,
        cursorUpdatedAt: String? = null,
        sortBy: String = "latest"
    ): Result<CommunityArticleListResponse> {
        return try {
            val response = communityService.getArticleList(boardIds, size, cursorId, cursorUpdatedAt, sortBy)
            if (response.isSuccess && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("게시글 목록 조회에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 인기 게시글 조회
    suspend fun getHotArticles(size: Int = 10): Result<CommunityArticleListResponse> {
        return try {
            val response = communityService.getHotArticles(size)
            if (response.isSuccess && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("인기 게시글 조회에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 게시글 상세 조회
    suspend fun getArticleDetail(articleId: String): Result<CommunityDetailResponse> {
        return try {
            val response = communityService.getArticleDetail(articleId)
            if (response.isSuccess && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("게시글 조회에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 게시글 작성 (이미지 포함)
    suspend fun createArticle(
        boardIds: Long,
        title: String,
        content: String,
        images: List<Uri> = emptyList()
    ): Result<ArticlePostResponse> {
        return try {
            // 이미지가 있으면 먼저 업로드
            val imageIds = if (images.isNotEmpty()) {
                val uploadResult = imageRepository.uploadImages(
                    imageUris = images,
                    purpose = ImageUploadPurpose.ARTICLE,
                    uploaderId = "user" // 서버에서 토큰으로 사용자 식별
                )
                if (uploadResult.isSuccess) {
                    uploadResult.getOrNull()?.map { it.imageId } ?: emptyList()
                } else {
                    return Result.failure(Exception("이미지 업로드에 실패했습니다."))
                }
            } else {
                emptyList()
            }

            val request = CreateArticleRequest(
                title = title,
                content = content,
                boardIds = boardIds,
                imageIds = imageIds
            )
            val response = communityService.createArticle(request)

            if (response.isSuccess && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("게시글 작성에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 게시글 삭제
    suspend fun deleteArticle(articleId: String): Result<Boolean> {
        return try {
            val response = communityService.deleteArticle(articleId)
            if (response.isSuccess) {
                Result.success(true)
            } else {
                Result.failure(Exception("게시글 삭제에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 게시글 좋아요
    suspend fun likeArticle(articleId: String): Result<Boolean> {
        return try {
            val userId = preferencesManager.getUserId()?.toString()
                ?: return Result.failure(Exception("로그인이 필요합니다."))
            val response = communityService.toggleLikeArticle(articleId, userId, true)
            // 204 No Content도 성공으로 처리
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("좋아요에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("좋아요에 실패했습니다."))
        }
    }

    // 게시글 좋아요 취소
    suspend fun unlikeArticle(articleId: String): Result<Boolean> {
        return try {
            val userId = preferencesManager.getUserId()?.toString()
                ?: return Result.failure(Exception("로그인이 필요합니다."))
            val response = communityService.toggleLikeArticle(articleId, userId, false)
            // 204 No Content도 성공으로 처리
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("좋아요 취소에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("좋아요 취소에 실패했습니다."))
        }
    }

    // 댓글 작성
    suspend fun createComment(
        articleId: String,
        contents: String,
        parentId: String? = null
    ): Result<CreateCommentResponse> {
        return try {
            val request = CreateCommentRequest(articleId = articleId, contents = contents)
            val response = communityService.createComment(request, parentId)
            if (response.isSuccess && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("댓글 작성에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 댓글 삭제
    suspend fun deleteComment(commentId: String): Result<Boolean> {
        return try {
            val response = communityService.deleteComment(commentId)
            if (response.isSuccess) {
                Result.success(true)
            } else {
                Result.failure(Exception("댓글 삭제에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 내가 좋아요한 게시글 목록 조회
    suspend fun getMyLikedArticles(
        size: Int = 20,
        cursor: String? = null
    ): Result<CommunityArticleListResponse> {
        return try {
            val response = communityService.getMyLikedArticles(size, cursor)
            if (response.isSuccess && response.result != null) {
                // MyFeedResponse를 CommunityArticleListResponse로 변환
                val pagedList = response.result.toPagedArticleList()
                Result.success(CommunityArticleListResponse(pagedList))
            } else {
                Result.failure(Exception("좋아요한 게시글 조회에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
