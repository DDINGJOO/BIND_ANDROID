package com.teambind.bind_android.data.repository

import com.teambind.bind_android.data.api.service.StudioService
import com.teambind.bind_android.data.model.response.PlaceDetailDto
import com.teambind.bind_android.data.model.response.RoomDetailResponse
import com.teambind.bind_android.data.model.response.RoomDto
import com.teambind.bind_android.data.model.response.RoomListResponse
import com.teambind.bind_android.data.model.response.StudioListResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudioRepository @Inject constructor(
    private val studioService: StudioService
) {

    // Place 목록 조회
    suspend fun getStudioList(
        province: String? = null,
        keyword: String? = null,
        keywordIds: String? = null,
        minPrice: Int? = null,
        maxPrice: Int? = null,
        date: String? = null,
        startTime: String? = null,
        endTime: String? = null,
        headCount: Int? = null,
        size: Int = 20,
        cursor: String? = null
    ): Result<StudioListResponse> {
        return try {
            val response = studioService.getStudioList(
                province = province,
                keyword = keyword,
                keywordIds = keywordIds,
                minPrice = minPrice,
                maxPrice = maxPrice,
                date = date,
                startTime = startTime,
                endTime = endTime,
                headCount = headCount,
                size = size,
                cursor = cursor
            )
            if (response.isSuccess && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("스튜디오 목록 조회에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Place 검색
    suspend fun searchStudios(
        query: String,
        size: Int = 20,
        cursorId: String? = null
    ): Result<StudioListResponse> {
        return try {
            val response = studioService.searchStudios(query, size, cursorId)
            if (response.isSuccess && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("스튜디오 검색에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Place 상세 조회
    suspend fun getPlaceDetail(placeId: String): Result<PlaceDetailDto> {
        return try {
            val response = studioService.getPlaceDetail(placeId)
            if (response.isSuccess && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("공간 상세 조회에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Room 상세 조회 (Place + Room + 가격정책 + 상품)
    suspend fun getRoomDetail(roomId: Long): Result<RoomDetailResponse> {
        return try {
            val response = studioService.getRoomDetail(roomId)
            if (response.isSuccess && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("연습실 상세 조회에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Place의 Room 목록 조회
    suspend fun getRoomsByPlace(placeId: String): Result<List<RoomDto>> {
        return try {
            val response = studioService.getRoomsByPlace(placeId)
            if (response.isSuccess && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("연습실 목록 조회에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 주변 Place 조회 (지도용)
    suspend fun getNearbyStudios(
        latitude: Double,
        longitude: Double,
        radius: Int = 5000
    ): Result<StudioListResponse> {
        return try {
            val response = studioService.getNearbyStudios(latitude, longitude, radius)
            if (response.isSuccess && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("주변 스튜디오 조회에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 인기 Place 조회
    suspend fun getPopularStudios(size: Int = 10): Result<StudioListResponse> {
        return try {
            val response = studioService.getPopularStudios(size)
            if (response.isSuccess && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("인기 스튜디오 조회에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Room 목록 조회
    suspend fun getRooms(): Result<RoomListResponse> {
        return try {
            val response = studioService.getRooms()
            if (response.isSuccess && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("연습실 목록 조회에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
