package com.teambind.bind_android.data.api.service

import com.teambind.bind_android.data.model.response.BaseResponse
import com.teambind.bind_android.data.model.response.InquiryDetailResponse
import com.teambind.bind_android.data.model.response.InquiryListResponse
import retrofit2.http.*

interface InquiryService {

    // 문의 작성 (서버: POST /bff/v1/inquiries)
    @POST("inquiries")
    suspend fun writeInquiry(
        @Body request: Map<String, String>
    ): BaseResponse<Boolean>

    // 내 문의 목록 조회 (서버: GET /bff/v1/inquiries/me)
    @GET("inquiries/me")
    suspend fun getMyInquiries(
        @Query("size") size: Int = 20,
        @Query("cursor") cursor: String? = null
    ): BaseResponse<InquiryListResponse>

    // 문의 상세 조회 (서버: GET /bff/v1/inquiries/{inquiryId})
    @GET("inquiries/{inquiryId}")
    suspend fun getInquiryDetail(
        @Path("inquiryId") inquiryId: String
    ): BaseResponse<InquiryDetailResponse>
}
