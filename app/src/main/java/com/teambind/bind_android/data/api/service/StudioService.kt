package com.teambind.bind_android.data.api.service

import com.teambind.bind_android.data.model.response.BaseResponse
import com.teambind.bind_android.data.model.response.PlaceDetailDto
import com.teambind.bind_android.data.model.response.RoomDetailResponse
import com.teambind.bind_android.data.model.response.RoomDto
import com.teambind.bind_android.data.model.response.RoomListResponse
import com.teambind.bind_android.data.model.response.StudioListResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface StudioService {

    // Place 상세 조회 (서버: GET /bff/v1/places/{placeId})
    @GET("places/{placeId}")
    suspend fun getPlaceDetail(
        @Path("placeId") placeId: String
    ): BaseResponse<PlaceDetailDto>

    // Place 검색 (서버: GET /bff/v1/places/search)
    @GET("places/search")
    suspend fun getStudioList(
        @Query("province") province: String? = null,
        @Query("keyword") keyword: String? = null,
        @Query("keywordIds") keywordIds: String? = null,
        @Query("minPrice") minPrice: Int? = null,
        @Query("maxPrice") maxPrice: Int? = null,
        @Query("date") date: String? = null,
        @Query("startTime") startTime: String? = null,
        @Query("endTime") endTime: String? = null,
        @Query("headCount") headCount: Int? = null,
        @Query("size") size: Int = 20,
        @Query("cursor") cursor: String? = null
    ): BaseResponse<StudioListResponse>

    // Place 검색 (서버: GET /bff/v1/places/search)
    @GET("places/search")
    suspend fun searchStudios(
        @Query("keyword") query: String,
        @Query("size") size: Int = 20,
        @Query("cursorId") cursorId: String? = null
    ): BaseResponse<StudioListResponse>

    // 주변 Place 조회 (서버: GET /bff/v1/places/nearby)
    @GET("places/nearby")
    suspend fun getNearbyStudios(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radiusInMeters") radius: Int = 5000
    ): BaseResponse<StudioListResponse>

    // Place 목록 조회 (서버: GET /bff/v1/places/search)
    @GET("places/search")
    suspend fun getPopularStudios(
        @Query("size") size: Int = 10
    ): BaseResponse<StudioListResponse>

    // 최근 조회한 Place (서버: GET /bff/v1/places/recent)
    @GET("places/recent")
    suspend fun getRecentStudios(
        @Query("size") size: Int = 10
    ): BaseResponse<StudioListResponse>

    // Room 상세 조회 (서버: GET /bff/v1/rooms/{roomId})
    @GET("rooms/{roomId}")
    suspend fun getRoomDetail(
        @Path("roomId") roomId: Long
    ): BaseResponse<RoomDetailResponse>

    // Room 목록 조회 (서버: GET /bff/v1/rooms/search)
    @GET("rooms/search")
    suspend fun getRooms(): BaseResponse<RoomListResponse>

    // Place의 Room 목록 (서버: GET /bff/v1/rooms/place/{placeId})
    // 서버가 data에 배열을 직접 반환하므로 List<RoomDto>로 받음
    @GET("rooms/place/{placeId}")
    suspend fun getRoomsByPlace(
        @Path("placeId") placeId: String
    ): BaseResponse<List<RoomDto>>
}
