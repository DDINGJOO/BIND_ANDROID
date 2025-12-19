package com.teambind.bind_android.data.api.service

import com.teambind.bind_android.data.model.response.BaseResponse
import com.teambind.bind_android.data.model.response.FaqDto
import com.teambind.bind_android.data.model.response.PlaceKeywordDto
import retrofit2.http.GET

interface EnumsService {

    // 지역 정보 조회 (서버: GET /bff/v1/enums/locations)
    @GET("enums/locations")
    suspend fun getRegions(): BaseResponse<Map<String, String>>

    // 게시글 키워드 목록 조회 (서버: GET /bff/v1/enums/articles/keywords)
    @GET("enums/articles/keywords")
    suspend fun getArticleKeywords(): BaseResponse<List<Any>>

    // 장소 키워드 목록 조회 (서버: GET /bff/v1/enums/place-keywords)
    @GET("enums/place-keywords")
    suspend fun getPlaceKeywords(): BaseResponse<List<PlaceKeywordDto>>

    // FAQ 목록 조회 (서버: GET /bff/v1/enums/faqs)
    @GET("enums/faqs")
    suspend fun getFaqs(): BaseResponse<List<FaqDto>>
}
