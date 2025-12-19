package com.teambind.bind_android.data.repository

import com.teambind.bind_android.data.api.service.StudioService
import com.teambind.bind_android.data.model.response.PlaceDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaceRepository @Inject constructor(
    private val studioService: StudioService
) {

    suspend fun searchPlaces(query: String): Result<List<PlaceDto>> {
        return try {
            val response = studioService.searchStudios(query, size = 20, cursorId = null)
            if (response.isSuccess && response.result != null) {
                val places = (response.result.studioList ?: emptyList()).map { studio ->
                    PlaceDto(
                        placeId = studio.studioId?.toLongOrNull() ?: 0L,
                        name = studio.name ?: "",
                        address = studio.address,
                        thumbnailUrl = studio.thumbnailUrl
                    )
                }
                Result.success(places)
            } else {
                Result.failure(Exception("검색에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
