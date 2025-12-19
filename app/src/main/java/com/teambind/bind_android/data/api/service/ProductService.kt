package com.teambind.bind_android.data.api.service

import com.teambind.bind_android.data.model.response.BaseResponse
import com.teambind.bind_android.data.model.response.ProductsAvailabilityResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ProductService {

    // 상품 가용성 조회 (서버: GET /bff/v1/products/availability)
    @GET("products/availability")
    suspend fun getProductsAvailability(
        @Query("roomId") roomId: Long,
        @Query("placeId") placeId: Long,
        @Query("timeSlots") timeSlots: String  // 콤마로 구분된 시간 슬롯 (예: "15:00:00,16:00:00")
    ): BaseResponse<ProductsAvailabilityResponse>
}
