package com.teambind.bind_android.data.repository

import com.teambind.bind_android.data.api.service.ProductService
import com.teambind.bind_android.data.model.response.PricingStrategyDto
import com.teambind.bind_android.data.model.response.ProductDto
import com.teambind.bind_android.data.model.response.ProductsAvailabilityResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val productService: ProductService
) {

    // 상품 가용성 조회 (ISO8601 형식으로 변환)
    // slotDate: "yyyy-MM-dd", timeSlots: ["HH:mm", "HH:mm"]
    suspend fun getProductsAvailability(
        roomId: Long,
        placeId: Long,
        slotDate: String,
        timeSlots: List<String>
    ): Result<ProductsAvailabilityResponse> {
        return try {
            // ISO8601 형식으로 변환: "2025-12-19T09:00:00"
            val isoTimeSlots = timeSlots.map { time -> "${slotDate}T${time}:00" }
            val timeSlotsParam = isoTimeSlots.joinToString(",")
            val response = productService.getProductsAvailability(roomId, placeId, timeSlotsParam)
            if (response.isSuccess && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("상품 가용성 조회에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 예약용 상품 목록 조회 (ProductDto 형식으로 반환)
    // slotDate: "yyyy-MM-dd", timeSlots: ["HH:mm", "HH:mm"]
    suspend fun getAvailableProducts(
        roomId: Long,
        placeId: Long,
        slotDate: String,
        timeSlots: List<String>
    ): Result<List<ProductDto>> {
        return try {
            // ISO8601 형식으로 변환: "2025-12-19T09:00:00"
            val isoTimeSlots = timeSlots.map { time -> "${slotDate}T${time}:00" }
            val timeSlotsParam = isoTimeSlots.joinToString(",")
            val response = productService.getProductsAvailability(roomId, placeId, timeSlotsParam)
            if (response.isSuccess && response.result != null) {
                val products = response.result.availableProducts.map { available ->
                    ProductDto(
                        productId = available.productId,
                        scope = null,
                        placeId = response.result.placeId,
                        roomId = roomId,
                        name = available.productName,
                        pricingStrategy = PricingStrategyDto(
                            pricingType = "FIXED",
                            initialPrice = available.unitPrice,
                            additionalPrice = null
                        ),
                        totalQuantity = available.availableQuantity
                    )
                }
                Result.success(products)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.success(emptyList())
        }
    }
}
