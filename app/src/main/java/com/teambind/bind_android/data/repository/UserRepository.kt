package com.teambind.bind_android.data.repository

import com.teambind.bind_android.data.api.service.CommunityService
import com.teambind.bind_android.data.api.service.ProfileService
import com.teambind.bind_android.data.model.response.CommunityArticleListResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val profileService: ProfileService,
    private val communityService: CommunityService
) {

    // 내가 작성한 게시글 목록
    suspend fun getMyArticles(
        size: Int = 20,
        cursorId: String? = null
    ): Result<CommunityArticleListResponse> {
        return try {
            val response = communityService.getMyArticles(size, cursorId)
            if (response.isSuccess && response.result != null) {
                // MyFeedResponse를 CommunityArticleListResponse로 변환
                val converted = CommunityArticleListResponse(
                    page = response.result.toPagedArticleList()
                )
                Result.success(converted)
            } else {
                Result.failure(Exception("내 게시글 조회에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 내가 댓글 작성한 게시글 목록
    suspend fun getMyCommentedArticles(
        size: Int = 20,
        cursorId: String? = null
    ): Result<CommunityArticleListResponse> {
        return try {
            val response = communityService.getMyCommentedArticles(size, cursorId)
            if (response.isSuccess && response.result != null) {
                // MyFeedResponse를 CommunityArticleListResponse로 변환
                val converted = CommunityArticleListResponse(
                    page = response.result.toPagedArticleList()
                )
                Result.success(converted)
            } else {
                Result.failure(Exception("댓글 작성 게시글 조회에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 내가 좋아요한 게시글 목록
    suspend fun getMyLikedArticles(
        size: Int = 20,
        cursorId: String? = null
    ): Result<CommunityArticleListResponse> {
        return try {
            val response = communityService.getMyLikedArticles(size, cursorId)
            if (response.isSuccess && response.result != null) {
                // MyFeedResponse를 CommunityArticleListResponse로 변환
                val converted = CommunityArticleListResponse(
                    page = response.result.toPagedArticleList()
                )
                Result.success(converted)
            } else {
                Result.failure(Exception("좋아요 게시글 조회에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 회원 탈퇴
    suspend fun deleteAccount(): Result<Boolean> {
        return try {
            val response = profileService.deleteAccount()
            if (response.isSuccess) {
                Result.success(true)
            } else {
                Result.failure(Exception("회원 탈퇴에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
