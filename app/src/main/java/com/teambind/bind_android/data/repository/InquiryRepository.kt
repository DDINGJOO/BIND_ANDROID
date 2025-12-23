package com.teambind.bind_android.data.repository

import com.teambind.bind_android.data.api.service.InquiryService
import com.teambind.bind_android.data.model.response.InquiryDetailResponse
import com.teambind.bind_android.data.model.response.InquiryListResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InquiryRepository @Inject constructor(
    private val inquiryService: InquiryService
) {

    // 문의 작성
    suspend fun writeInquiry(
        category: String,
        title: String,
        content: String
    ): Result<Boolean> {
        return try {
            val request = mapOf(
                "category" to category,
                "title" to title,
                "content" to content
            )
            val response = inquiryService.writeInquiry(request)
            if (response.isSuccess) {
                Result.success(true)
            } else {
                Result.failure(Exception("문의 접수에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("문의 접수에 실패했습니다."))
        }
    }

    // 내 문의 목록 조회
    suspend fun getMyInquiries(
        size: Int = 20,
        cursor: String? = null
    ): Result<InquiryListResponse> {
        return try {
            val response = inquiryService.getMyInquiries(size, cursor)
            if (response.isSuccess && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("문의 목록 조회에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 문의 상세 조회
    suspend fun getInquiryDetail(inquiryId: String): Result<InquiryDetailResponse> {
        return try {
            val response = inquiryService.getInquiryDetail(inquiryId)
            if (response.isSuccess && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("문의 상세 조회에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
