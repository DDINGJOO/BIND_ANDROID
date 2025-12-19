package com.teambind.bind_android.presentation.reservationoption

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teambind.bind_android.data.model.response.ProductDto
import com.teambind.bind_android.data.model.response.ReservationPreviewResponse
import com.teambind.bind_android.data.repository.ProductRepository
import com.teambind.bind_android.data.repository.ReservationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

data class SelectedProduct(
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val unitPrice: Int
)

data class ReservationOptionUiState(
    val isLoading: Boolean = false,
    val reservationId: Long = 0L,
    val roomId: Long = 0L,
    val placeId: Long = 0L,
    val roomName: String = "",
    val roomImageUrl: String? = null,
    val selectedDate: String = "",
    val displayDate: String = "",
    val selectedTimes: List<String> = emptyList(),
    val displayTimeRange: String = "",
    val minUnit: Int = 60,
    val roomPrice: Int = 0,
    val productPrice: Int = 0,
    val totalPrice: Int = 0,
    val displayPrice: String = "",
    val availableProducts: List<ProductDto> = emptyList(),
    val selectedProducts: List<SelectedProduct> = emptyList(),
    val productOptionText: String = "상품 옵션 선택",
    val errorMessage: String? = null
)

sealed class ReservationOptionEvent {
    object ShowProductSelector : ReservationOptionEvent()
    data class NavigateToReservationForm(
        val reservationId: Long,
        val roomId: Long,
        val roomName: String,
        val roomImageUrl: String?,
        val selectedDate: String,
        val selectedTimes: List<String>,
        val roomPrice: Int,
        val optionPrice: Int,
        val totalPrice: Int
    ) : ReservationOptionEvent()
    object NavigateBack : ReservationOptionEvent()
}

@HiltViewModel
class ReservationOptionViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReservationOptionUiState())
    val uiState: StateFlow<ReservationOptionUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ReservationOptionEvent>()
    val events = _events.asSharedFlow()

    fun initialize(
        reservationId: Long,
        roomId: Long,
        placeId: Long,
        roomName: String,
        roomImageUrl: String?,
        selectedDate: String,
        selectedTimes: List<String>,
        minUnit: Int = 60,
        roomPrice: Int = 0
    ) {
        val formatter = NumberFormat.getNumberInstance(Locale.KOREA)
        _uiState.value = _uiState.value.copy(
            reservationId = reservationId,
            roomId = roomId,
            placeId = placeId,
            roomName = roomName,
            roomImageUrl = roomImageUrl,
            selectedDate = selectedDate,
            selectedTimes = selectedTimes,
            minUnit = minUnit,
            roomPrice = roomPrice,
            totalPrice = roomPrice,
            displayPrice = "${formatter.format(roomPrice)}원",
            displayDate = formatDisplayDate(selectedDate),
            displayTimeRange = formatTimeRange(selectedTimes, minUnit)
        )
        loadAvailableProducts()
    }

    private fun formatDisplayDate(dateString: String): String {
        return try {
            val parts = dateString.split("-")
            if (parts.size == 3) {
                val year = parts[0].takeLast(2)
                val month = parts[1]
                val day = parts[2]
                val dayOfWeek = getDayOfWeek(dateString)
                "$year.$month.$day ($dayOfWeek)"
            } else {
                dateString
            }
        } catch (e: Exception) {
            dateString
        }
    }

    private fun getDayOfWeek(dateString: String): String {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.KOREA)
            val date = sdf.parse(dateString)
            val cal = java.util.Calendar.getInstance()
            cal.time = date!!
            val dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK)
            when (dayOfWeek) {
                java.util.Calendar.SUNDAY -> "일"
                java.util.Calendar.MONDAY -> "월"
                java.util.Calendar.TUESDAY -> "화"
                java.util.Calendar.WEDNESDAY -> "수"
                java.util.Calendar.THURSDAY -> "목"
                java.util.Calendar.FRIDAY -> "금"
                java.util.Calendar.SATURDAY -> "토"
                else -> ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    private fun formatTimeRange(times: List<String>, minUnit: Int): String {
        if (times.isEmpty()) return ""
        val startTime = times.first()
        val endTime = calculateEndTime(times.last(), minUnit)

        // 총 시간 계산: 선택된 셀 수 × minUnit (분)
        val totalMinutes = times.size * minUnit
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        val totalTimeText = if (minutes == 0) {
            "${hours}시간"
        } else {
            "${hours}.${minutes * 10 / 60}시간"
        }

        return "$startTime ~ $endTime (총 $totalTimeText)"
    }

    private fun calculateEndTime(startTime: String, minUnit: Int): String {
        return try {
            val parts = startTime.split(":")
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()

            var totalMinutes = hour * 60 + minute + minUnit
            if (totalMinutes >= 24 * 60) {
                totalMinutes -= 24 * 60
            }

            val endHour = totalMinutes / 60
            val endMinute = totalMinutes % 60

            String.format(java.util.Locale.US, "%02d:%02d", endHour, endMinute)
        } catch (e: Exception) {
            startTime
        }
    }

    // 클라이언트에서 가격 계산 (iOS와 동일한 방식)
    private fun updateTotalPrice() {
        val currentState = _uiState.value
        val formatter = NumberFormat.getNumberInstance(Locale.KOREA)

        // 상품 가격 합계
        val productPrice = currentState.selectedProducts.sumOf { it.unitPrice * it.quantity }

        // 총 가격 = 룸 가격 + 상품 가격
        val totalPrice = currentState.roomPrice + productPrice

        _uiState.value = currentState.copy(
            productPrice = productPrice,
            totalPrice = totalPrice,
            displayPrice = "${formatter.format(totalPrice)}원"
        )
    }

    private fun loadAvailableProducts() {
        val currentState = _uiState.value
        viewModelScope.launch {
            productRepository.getAvailableProducts(
                roomId = currentState.roomId,
                placeId = currentState.placeId,
                slotDate = currentState.selectedDate,
                timeSlots = currentState.selectedTimes
            )
                .onSuccess { products ->
                    _uiState.value = _uiState.value.copy(availableProducts = products)
                }
                .onFailure { /* Ignore - products are optional */ }
        }
    }

    fun onSelectProductClick() {
        viewModelScope.launch {
            _events.emit(ReservationOptionEvent.ShowProductSelector)
        }
    }

    fun updateSelectedProducts(products: List<SelectedProduct>) {
        val productOptionText = if (products.isEmpty()) {
            "상품 옵션 선택"
        } else {
            products.joinToString(", ") { "${it.productName} 추가 X ${it.quantity}" }
        }

        _uiState.value = _uiState.value.copy(
            selectedProducts = products,
            productOptionText = productOptionText
        )
        updateTotalPrice()
    }

    fun onConfirmClick() {
        val currentState = _uiState.value
        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true)

            // 상품이 선택된 경우 상품 업데이트 API 호출 (iOS와 동일)
            if (currentState.selectedProducts.isNotEmpty()) {
                val productQuantities = currentState.selectedProducts.map {
                    com.teambind.bind_android.data.model.request.ProductQuantity(
                        productId = it.productId,
                        quantity = it.quantity
                    )
                }
                reservationRepository.updateReservationProducts(
                    reservationId = currentState.reservationId,
                    products = productQuantities
                ).onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "상품 업데이트에 실패했습니다."
                    )
                    return@launch
                }
            }

            _uiState.value = _uiState.value.copy(isLoading = false)

            // ReservationForm으로 이동 (예약은 이미 SelectTime에서 생성됨)
            _events.emit(
                ReservationOptionEvent.NavigateToReservationForm(
                    reservationId = currentState.reservationId,
                    roomId = currentState.roomId,
                    roomName = currentState.roomName,
                    roomImageUrl = currentState.roomImageUrl,
                    selectedDate = currentState.selectedDate,
                    selectedTimes = currentState.selectedTimes,
                    roomPrice = currentState.roomPrice,
                    optionPrice = currentState.productPrice,
                    totalPrice = currentState.totalPrice
                )
            )
        }
    }

    fun onBackClick() {
        viewModelScope.launch {
            _events.emit(ReservationOptionEvent.NavigateBack)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
