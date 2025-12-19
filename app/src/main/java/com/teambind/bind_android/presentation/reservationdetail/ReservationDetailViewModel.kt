package com.teambind.bind_android.presentation.reservationdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teambind.bind_android.data.model.response.MyReservationDetailResponse
import com.teambind.bind_android.data.model.response.SelectedProductDto
import com.teambind.bind_android.data.repository.ReservationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject

enum class ReservationDetailStatus(val value: String, val displayName: String) {
    AWAITING_USER_INFO("AWAITING_USER_INFO", "정보 입력 대기"),
    PENDING("PENDING", "대기중"),
    PENDING_CONFIRMED("PENDING_CONFIRMED", "승인 대기"),
    PENDING_PAYMENT("PENDING_PAYMENT", "결제 대기"),
    CONFIRMED("CONFIRMED", "예약 확정"),
    REJECTED("REJECTED", "거절됨"),
    REFUNDED("REFUNDED", "환불 완료"),
    CANCELLED("CANCELLED", "취소됨"),
    COMPLETED("COMPLETED", "이용 완료")
}

data class ReservationDetailUiState(
    val isLoading: Boolean = false,
    val reservationId: Long = 0L,
    val roomId: Long = 0L,
    val detail: MyReservationDetailResponse? = null,
    val statusDisplay: String = "",
    val placeName: String = "",
    val placeAddress: String = "",
    val roomName: String = "",
    val roomImageUrl: String? = null,
    val dateDisplay: String = "",
    val timeDisplay: String = "",
    val reserverName: String = "",
    val reserverPhone: String = "",
    val additionalInfo: Map<String, String> = emptyMap(),
    val products: List<SelectedProductDto> = emptyList(),
    val roomPriceDisplay: String = "0원",
    val optionPriceDisplay: String = "0원",
    val totalPriceDisplay: String = "0원",
    val canCancel: Boolean = false,
    val canRefund: Boolean = false,
    val cancelButtonText: String = "취소 요청",
    val errorMessage: String? = null
)

sealed class ReservationDetailEvent {
    data class ShowMessage(val message: String) : ReservationDetailEvent()
    object NavigateBack : ReservationDetailEvent()
    object RefreshList : ReservationDetailEvent()
}

@HiltViewModel
class ReservationDetailViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReservationDetailUiState())
    val uiState: StateFlow<ReservationDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ReservationDetailEvent>()
    val events = _events.asSharedFlow()

    fun loadDetail(reservationId: Long) {
        _uiState.value = _uiState.value.copy(reservationId = reservationId, isLoading = true)

        viewModelScope.launch {
            reservationRepository.getMyReservationDetail(reservationId)
                .onSuccess { detail ->
                    updateUiState(detail)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "예약 상세 조회에 실패했습니다."
                    )
                }
        }
    }

    private fun updateUiState(detail: MyReservationDetailResponse) {
        val formatter = NumberFormat.getNumberInstance(Locale.KOREA)
        val status = ReservationDetailStatus.entries.find { it.value == detail.status }
            ?: ReservationDetailStatus.PENDING

        val roomPrice = detail.reservationTimePrice
        val optionPrice = detail.totalPrice - detail.reservationTimePrice

        // 취소/환불 버튼 상태 결정
        val canCancel = status == ReservationDetailStatus.PENDING_CONFIRMED
        val canRefund = status == ReservationDetailStatus.CONFIRMED || status == ReservationDetailStatus.REJECTED
        val cancelButtonText = if (canRefund) "환불 요청" else "취소 요청"

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            roomId = detail.roomInfo?.roomId ?: 0L,
            detail = detail,
            statusDisplay = status.displayName,
            placeName = detail.placeName ?: "장소",
            placeAddress = detail.placeAddress ?: "",
            roomName = detail.roomName ?: "룸",
            roomImageUrl = detail.firstImageUrl,
            dateDisplay = formatDisplayDate(detail.reservationDate),
            timeDisplay = formatTimeRange(detail.startTimes ?: emptyList()),
            reserverName = detail.reserverName ?: "",
            reserverPhone = detail.reserverPhone ?: "",
            additionalInfo = detail.additionalInfo?.mapValues { it.value.toString() } ?: emptyMap(),
            products = detail.selectedProducts ?: emptyList(),
            roomPriceDisplay = "${formatter.format(roomPrice)}원",
            optionPriceDisplay = "${formatter.format(optionPrice)}원",
            totalPriceDisplay = "${formatter.format(detail.totalPrice)}원",
            canCancel = canCancel,
            canRefund = canRefund,
            cancelButtonText = cancelButtonText
        )
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
            when (cal.get(java.util.Calendar.DAY_OF_WEEK)) {
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

    private fun formatTimeRange(times: List<String>): String {
        if (times.isEmpty()) return ""
        val startTime = times.first()
        val endTime = addOneHour(times.last())
        val totalHours = times.size
        return "$startTime ~ $endTime (${totalHours}시간)"
    }

    private fun addOneHour(startTime: String): String {
        return try {
            val parts = startTime.split(":")
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()
            var endHour = hour + 1
            if (endHour >= 24) endHour = 0
            String.format("%02d:%02d", endHour, minute)
        } catch (e: Exception) {
            startTime
        }
    }

    fun onCancelOrRefundClick() {
        val currentState = _uiState.value
        if (currentState.isLoading) return

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true)

            val result = if (currentState.canRefund) {
                reservationRepository.requestRefund(currentState.reservationId)
            } else {
                reservationRepository.cancelPayment(currentState.reservationId)
            }

            result
                .onSuccess { message ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(ReservationDetailEvent.ShowMessage(message))
                    _events.emit(ReservationDetailEvent.RefreshList)
                    _events.emit(ReservationDetailEvent.NavigateBack)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(ReservationDetailEvent.ShowMessage(error.message ?: "요청 처리에 실패했습니다."))
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
