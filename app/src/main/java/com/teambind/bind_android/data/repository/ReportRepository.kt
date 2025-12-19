package com.teambind.bind_android.data.repository

import com.teambind.bind_android.data.api.service.ReportService
import com.teambind.bind_android.data.model.request.ReferenceType
import com.teambind.bind_android.data.model.request.ReportCreateRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepository @Inject constructor(
    private val reportService: ReportService
) {

    suspend fun createReport(
        reporterId: String,
        reportedId: String,
        referenceType: ReferenceType,
        reportCategory: String,
        reason: String
    ): Result<Unit> {
        return try {
            val request = ReportCreateRequest(
                reporterId = reporterId,
                reportedId = reportedId,
                referenceType = referenceType,
                reportCategory = reportCategory,
                reason = reason
            )
            val response = reportService.createReport(request)
            if (response.isSuccess) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("신고 등록에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
