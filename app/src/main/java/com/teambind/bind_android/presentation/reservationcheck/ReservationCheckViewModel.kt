package com.teambind.bind_android.presentation.reservationcheck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teambind.bind_android.data.repository.ReservationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject

data class ReservationCheckUiState(
    val isLoading: Boolean = false,
    val reservationId: Long = 0L,
    val studioImageUrl: String? = null,
    val studioName: String = "",
    val studioAddress: String = "",
    val reservationDate: String = "",
    val reservationTime: String = "",
    val productOptions: String = "없음",
    val reservistName: String = "",
    val reservistContact: String = "",
    val roomPrice: String = "0원",
    val optionPrice: String = "0원",
    val totalPrice: Int = 0,
    val displayTotalPrice: String = "총 0원 결제하기",
    val errorMessage: String? = null
)

sealed class ReservationCheckEvent {
    data class NavigateToPayment(
        val reservationId: Long,
        val totalPrice: Int
    ) : ReservationCheckEvent()

    object Dismiss : ReservationCheckEvent()
    data class ShowError(val message: String) : ReservationCheckEvent()
}

@HiltViewModel
class ReservationCheckViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReservationCheckUiState())
    val uiState: StateFlow<ReservationCheckUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ReservationCheckEvent>()
    val events = _events.asSharedFlow()

    fun initialize(
        reservationId: Long,
        roomName: String,
        roomImageUrl: String?,
        selectedDate: String,
        selectedTimes: List<String>,
        totalPrice: Int,
        reservistName: String = "",
        reservistContact: String = ""
    ) {
        val formatter = NumberFormat.getNumberInstance(Locale.KOREA)
        _uiState.value = _uiState.value.copy(
            reservationId = reservationId,
            studioName = roomName,
            studioImageUrl = roomImageUrl,
            reservationDate = formatDisplayDate(selectedDate),
            reservationTime = formatTimeRange(selectedTimes),
            reservistName = reservistName,
            reservistContact = reservistContact,
            totalPrice = totalPrice,
            displayTotalPrice = "총 ${formatter.format(totalPrice)}원 결제하기"
        )

        loadReservationDetail()
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
        val endTime = calculateEndTime(times.last())
        val totalHours = times.size * 0.5
        return if (totalHours == totalHours.toInt().toDouble()) {
            "$startTime ~ $endTime (총 ${totalHours.toInt()}시간)"
        } else {
            "$startTime ~ $endTime (총 ${totalHours}시간)"
        }
    }

    private fun calculateEndTime(startTime: String): String {
        return try {
            val parts = startTime.split(":")
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()
            var endMinute = minute + 30
            var endHour = hour
            if (endMinute >= 60) {
                endMinute -= 60
                endHour += 1
            }
            if (endHour >= 24) endHour = 0
            String.format("%02d:%02d", endHour, endMinute)
        } catch (e: Exception) {
            startTime
        }
    }

    private fun loadReservationDetail() {
        val currentState = _uiState.value
        viewModelScope.launch {
            reservationRepository.getMyReservationDetail(currentState.reservationId)
                .onSuccess { detail ->
                    val formatter = NumberFormat.getNumberInstance(Locale.KOREA)
                    val roomPrice = detail.reservationTimePrice
                    val productPrice = detail.totalPrice - detail.reservationTimePrice
                    _uiState.value = _uiState.value.copy(
                        reservistName = detail.reserverName ?: _uiState.value.reservistName,
                        reservistContact = detail.reserverPhone ?: _uiState.value.reservistContact,
                        roomPrice = "${formatter.format(roomPrice)}원",
                        optionPrice = "${formatter.format(productPrice)}원",
                        totalPrice = detail.totalPrice,
                        displayTotalPrice = "총 ${formatter.format(detail.totalPrice)}원 결제하기"
                    )
                }
                .onFailure { /* Use initial values */ }
        }
    }

    fun onConfirmClick() {
        val currentState = _uiState.value
        viewModelScope.launch {
            _events.emit(
                ReservationCheckEvent.NavigateToPayment(
                    reservationId = currentState.reservationId,
                    totalPrice = currentState.totalPrice
                )
            )
        }
    }

    fun onCloseClick() {
        viewModelScope.launch {
            _events.emit(ReservationCheckEvent.Dismiss)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
