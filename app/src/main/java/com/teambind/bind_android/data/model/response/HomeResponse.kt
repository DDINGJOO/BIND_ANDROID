package com.teambind.bind_android.data.model.response

import com.google.gson.annotations.SerializedName

// 배너 응답
data class BannerResponse(
    @SerializedName("banners")
    val banners: List<BannerDto>
)

data class BannerDto(
    @SerializedName("bannerId")
    val bannerId: String,
    @SerializedName("imageUrl")
    val imageUrl: String,
    @SerializedName("linkUrl")
    val linkUrl: String?,
    @SerializedName("title")
    val title: String?
)

// 인기 게시글 응답 (Community와 동일한 구조 사용)
data class HotPostsResponse(
    @SerializedName("posts")
    val posts: List<ArticleDto>
)

// 장소/스튜디오 목록 응답 (서버 응답: data 안에 바로 items가 있음)
data class StudioListResponse(
    @SerializedName("items")
    val studioList: List<StudioDto>?,
    @SerializedName("nextCursor")
    val nextCursor: String?,
    @SerializedName("hasNext")
    val hasNext: Boolean?,
    @SerializedName("count")
    val count: Int?,
    @SerializedName("metadata")
    val metadata: PlaceMetaData?
)

data class PlaceMetaData(
    @SerializedName("searchTime")
    val searchTime: Int?,
    @SerializedName("sortBy")
    val sortBy: String?,
    @SerializedName("sortDirection")
    val sortDirection: String?,
    @SerializedName("centerLat")
    val centerLat: Double?,
    @SerializedName("centerLng")
    val centerLng: Double?,
    @SerializedName("radiusInMeters")
    val radiusInMeters: Int?,
    @SerializedName("appliedFilters")
    val appliedFilters: String?
)

data class StudioDto(
    @SerializedName("id")
    val studioId: String?,
    @SerializedName("placeName")
    val name: String?,
    @SerializedName("fullAddress")
    val address: String?,
    @SerializedName("category")
    val category: String?,
    @SerializedName("placeType")
    val placeType: String?,
    @SerializedName("thumbnailUrl")
    val thumbnailUrl: String?,
    @SerializedName("ratingAverage")
    val rating: Double?,
    @SerializedName("reviewCount")
    val reviewCount: Int?,
    @SerializedName("latitude")
    val latitude: Double?,
    @SerializedName("longitude")
    val longitude: Double?,
    @SerializedName("distance")
    val distance: Double?,
    @SerializedName("distanceInKm")
    val distanceInKm: Double?,
    @SerializedName("formattedDistance")
    val formattedDistance: String?,
    @SerializedName("keywords")
    val keywords: List<String>?,
    @SerializedName("contact")
    val contact: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("roomCount")
    val roomCount: Int?,
    @SerializedName("roomIds")
    val roomIds: List<Long>?,
    @SerializedName("parkingAvailable")
    val parkingAvailable: Boolean?,
    @SerializedName("parkingType")
    val parkingType: String?,
    @SerializedName("isActive")
    val isActive: Boolean?,
    @SerializedName("approvalStatus")
    val approvalStatus: String?
)

// Room 상세 응답 (서버: GET /bff/v1/rooms/{roomId})
data class RoomDetailResponse(
    @SerializedName("room")
    val room: RoomDetailDto?,
    @SerializedName("place")
    val place: PlaceDetailDto?,
    @SerializedName("pricingPolicy")
    val pricingPolicy: PricingPolicyDto?,
    @SerializedName("availableProducts")
    val availableProducts: List<ProductDto>?
)

data class RoomDetailDto(
    @SerializedName("roomId")
    val roomId: Long,
    @SerializedName("roomName")
    val roomName: String,
    @SerializedName("placeId")
    val placeId: Long,
    @SerializedName("status")
    val status: String?,
    @SerializedName("timeSlot")
    val timeSlot: String?,
    @SerializedName("maxOccupancy")
    val maxOccupancy: Int?,
    @SerializedName("furtherDetails")
    val furtherDetails: List<String>?,
    @SerializedName("cautionDetails")
    val cautionDetails: List<String>?,
    @SerializedName("images")
    val images: List<RoomImageDto>?,
    @SerializedName("imageUrls")
    val imageUrls: List<String>?,
    @SerializedName("keywordIds")
    val keywordIds: List<Long>?
)

data class RoomImageDto(
    @SerializedName("imageId")
    val imageId: String,
    @SerializedName("imageUrl")
    val imageUrl: String,
    @SerializedName("sequence")
    val sequence: Int
)

data class PlaceDetailDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("userId")
    val userId: String?,
    @SerializedName("placeName")
    val placeName: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("category")
    val category: String?,
    @SerializedName("placeType")
    val placeType: String?,
    @SerializedName("contact")
    val contact: PlaceContactDto?,
    @SerializedName("location")
    val location: PlaceLocationDto?,
    @SerializedName("parking")
    val parking: PlaceParkingDto?,
    @SerializedName("images")
    val images: List<RoomImageDto>?,
    @SerializedName("imageUrls")
    val imageUrls: List<String>?,
    @SerializedName("keywords")
    val keywords: List<String>?,
    @SerializedName("isActive")
    val isActive: Boolean?,
    @SerializedName("approvalStatus")
    val approvalStatus: String?,
    @SerializedName("ratingAverage")
    val ratingAverage: Double?,
    @SerializedName("reviewCount")
    val reviewCount: Int?,
    @SerializedName("roomCount")
    val roomCount: Int?,
    @SerializedName("roomIds")
    val roomIds: List<Long>?,
    @SerializedName("createdAt")
    val createdAt: String?,
    @SerializedName("updatedAt")
    val updatedAt: String?
)

data class PlaceContactDto(
    @SerializedName("contact")
    val contact: String?,
    @SerializedName("email")
    val email: String?,
    @SerializedName("websites")
    val websites: List<String>?,
    @SerializedName("socialLinks")
    val socialLinks: List<String>?
)

data class PlaceLocationDto(
    @SerializedName("address")
    val address: PlaceAddressDto?,
    @SerializedName("latitude")
    val latitude: Double?,
    @SerializedName("longitude")
    val longitude: Double?,
    @SerializedName("locationGuide")
    val locationGuide: String?
)

data class PlaceAddressDto(
    @SerializedName("province")
    val province: String?,
    @SerializedName("city")
    val city: String?,
    @SerializedName("district")
    val district: String?,
    @SerializedName("fullAddress")
    val fullAddress: String?,
    @SerializedName("addressDetail")
    val addressDetail: String?,
    @SerializedName("postalCode")
    val postalCode: String?,
    @SerializedName("shortAddress")
    val shortAddress: String?
)

data class PlaceParkingDto(
    @SerializedName("available")
    val available: Boolean?,
    @SerializedName("parkingType")
    val parkingType: String?,
    @SerializedName("description")
    val description: String?
)

data class PricingPolicyDto(
    @SerializedName("roomId")
    val roomId: Long?,
    @SerializedName("placeId")
    val placeId: Long?,
    @SerializedName("timeSlot")
    val timeSlot: String?,
    @SerializedName("defaultPrice")
    val defaultPrice: Int?,
    @SerializedName("timeRangePrices")
    val timeRangePrices: List<TimeRangePriceDto>?
)

data class TimeRangePriceDto(
    @SerializedName("dayOfWeek")
    val dayOfWeek: String?,
    @SerializedName("startTime")
    val startTime: String?,
    @SerializedName("endTime")
    val endTime: String?,
    @SerializedName("price")
    val price: Int?
)

data class ProductDto(
    @SerializedName("productId")
    val productId: Long,
    @SerializedName("scope")
    val scope: String?,
    @SerializedName("placeId")
    val placeId: Long?,
    @SerializedName("roomId")
    val roomId: Long?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("pricingStrategy")
    val pricingStrategy: PricingStrategyDto?,
    @SerializedName("totalQuantity")
    val totalQuantity: Int?
)

data class PricingStrategyDto(
    @SerializedName("pricingType")
    val pricingType: String?,
    @SerializedName("initialPrice")
    val initialPrice: Int?,
    @SerializedName("additionalPrice")
    val additionalPrice: Int?
)

// 기존 RoomDto (목록용) - deprecated, use RoomDetailDto for detail
data class RoomDto(
    @SerializedName("roomId")
    val roomId: Long,
    @SerializedName("roomName")
    val name: String,
    @SerializedName("placeId")
    val placeId: Long?,
    @SerializedName("timeSlot")
    val timeSlot: String?,
    @SerializedName("maxOccupancy")
    val maxOccupancy: Int?,
    @SerializedName("images")
    val images: List<RoomImageDto>?,
    @SerializedName("imageUrls")
    val imageUrls: List<String>?,
    @SerializedName("keywordIds")
    val keywordIds: List<Long>?
)

// Room 목록 응답 (서버: GET /bff/v1/rooms/search)
data class RoomListResponse(
    @SerializedName("items")
    val rooms: List<RoomDto>?
)

// 기존 StudioDetailResponse - deprecated
data class StudioDetailResponse(
    @SerializedName("studio")
    val studio: StudioDetailDto?,
    @SerializedName("rooms")
    val rooms: List<RoomDto>?
)

data class StudioDetailDto(
    @SerializedName("studioId")
    val studioId: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("address")
    val address: String?,
    @SerializedName("city")
    val city: String?,
    @SerializedName("phoneNumber")
    val phoneNumber: String?,
    @SerializedName("operatingHours")
    val operatingHours: String?,
    @SerializedName("images")
    val images: List<ImageDto>?,
    @SerializedName("rating")
    val rating: Double?,
    @SerializedName("reviewCount")
    val reviewCount: Int?,
    @SerializedName("latitude")
    val latitude: Double?,
    @SerializedName("longitude")
    val longitude: Double?,
    @SerializedName("amenities")
    val amenities: List<String>?,
    @SerializedName("parkingInfo")
    val parkingInfo: String?
)

// 예약 응답
data class ReservationListResponse(
    @SerializedName("reservations")
    val reservations: List<ReservationDto>
)

data class ReservationDto(
    @SerializedName("reservationId")
    val reservationId: String,
    @SerializedName("studioName")
    val studioName: String,
    @SerializedName("roomName")
    val roomName: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("startTime")
    val startTime: String,
    @SerializedName("endTime")
    val endTime: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("totalPrice")
    val totalPrice: Int,
    @SerializedName("thumbnailUrl")
    val thumbnailUrl: String?
)

// Place DTO for search results
data class PlaceDto(
    val placeId: Long,
    val name: String,
    val address: String?,
    val thumbnailUrl: String?
)

// Place 상세 응답 (서버: GET /bff/v1/places/{placeId})
data class PlaceDetailResponse(
    @SerializedName("place")
    val place: PlaceDetailDto?,
    @SerializedName("rooms")
    val rooms: List<RoomDto>?
)

// FAQ 응답 (서버: GET /bff/v1/enums/faqs)
data class FaqDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("category")
    val category: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("question")
    val question: String,
    @SerializedName("answer")
    val answer: String,
    @SerializedName("createdAt")
    val createdAt: String?,
    @SerializedName("updatedAt")
    val updatedAt: String?
) {
    fun getCategoryDisplayName(): String {
        return when (category) {
            "ALL" -> "전체"
            "RESERVATION" -> "예약"
            "CHECK_IN" -> "체크인"
            "PAYMENT" -> "결제"
            "REVIEW_REPORT" -> "리뷰/신고"
            "ETC" -> "기타"
            else -> category
        }
    }
}

