package com.teambind.bind_android.presentation.selecttime

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teambind.bind_android.data.repository.AuthRepository
import com.teambind.bind_android.data.repository.PricingPolicyRepository
import com.teambind.bind_android.data.repository.ReservationRepository
import com.teambind.bind_android.presentation.selecttime.adapter.TimeSlotItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SelectTimeUiState(
    val isLoading: Boolean = false,
    val roomId: Long = 0L,
    val placeId: Long = 0L,
    val roomName: String = "",
    val roomImageUrl: String? = null,
    val selectedDate: String = "",
    val displayDate: String = "",
    val minUnit: Int = 60,
    val timeSlots: List<TimeSlotItem> = emptyList(),
    val selectedIndices: List<Int> = emptyList(),
    val selectedTimeRange: String = "",
    val totalPrice: Int = 0,
    val canConfirm: Boolean = false,
    val errorMessage: String? = null,
    val firstSelectedIndex: Int? = null,
    val isSelectingRange: Boolean = false
)

sealed class SelectTimeEvent {
    data class NavigateToReservationOption(
        val reservationId: Long,
        val roomId: Long,
        val placeId: Long,
        val roomName: String,
        val roomImageUrl: String?,
        val selectedDate: String,
        val selectedTimes: List<String>,
        val minUnit: Int,
        val roomPrice: Int
    ) : SelectTimeEvent()

    object Dismiss : SelectTimeEvent()
    object ShowUnavailableAlert : SelectTimeEvent()
    object RequirePhoneVerification : SelectTimeEvent()
}

@HiltViewModel
class SelectTimeViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository,
    private val pricingPolicyRepository: PricingPolicyRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(SelectTimeUiState())
    val uiState: StateFlow<SelectTimeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SelectTimeEvent>()
    val events = _events.asSharedFlow()

    fun initialize(
        roomId: Long,
        placeId: Long,
        roomName: String,
        roomImageUrl: String?,
        selectedDate: String,
        minUnit: Int
    ) {
        _uiState.value = _uiState.value.copy(
            roomId = roomId,
            placeId = placeId,
            roomName = roomName,
            roomImageUrl = roomImageUrl,
            selectedDate = selectedDate,
            displayDate = formatDisplayDate(selectedDate),
            minUnit = minUnit
        )
        loadAvailableSlots()
    }

    private fun formatDisplayDate(dateString: String): String {
        return try {
            val parts = dateString.split("-")
            if (parts.size == 3) {
                val month = parts[1].toInt()
                val day = parts[2].toInt()
                val dayOfWeek = getDayOfWeek(dateString)
                "${month}월 ${day}일 ($dayOfWeek)"
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

    private fun loadAvailableSlots() {
        val currentState = _uiState.value
        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true)

            // iOS와 동일: 예약 가능 슬롯과 가격 정책을 동시에 조회
            val slotsDeferred = async {
                reservationRepository.getAvailableSlots(currentState.roomId, currentState.selectedDate)
            }
            val pricingDeferred = async {
                pricingPolicyRepository.getRoomPricingPolicyByDate(
                    currentState.roomId,
                    currentState.selectedDate
                )
            }

            val slotsResult = slotsDeferred.await()
            val pricingResult = pricingDeferred.await()

            // 가격 정보 맵 (HH:mm -> price)
            val priceMap: Map<String, Int> = pricingResult.getOrNull()?.timeSlotPrices ?: emptyMap()

            slotsResult
                .onSuccess { slots ->
                    // iOS처럼 minUnit에 맞는 슬롯만 필터링
                    // HOUR(60분): 정시(:00)만 표시
                    // HALF_HOUR(30분): 모든 슬롯 표시
                    val filteredSlots = if (currentState.minUnit == 60) {
                        slots.filter { slot ->
                            try {
                                val minute = slot.slotTime.split(":")[1].toInt()
                                minute == 0
                            } catch (e: Exception) {
                                false
                            }
                        }
                    } else {
                        slots
                    }

                    // 오늘 날짜인 경우 현재 시각 이전 슬롯은 예약 불가 처리
                    val isToday = isSelectedDateToday(currentState.selectedDate)
                    val currentTimeMinutes = if (isToday) getCurrentTimeMinutes() else 0

                    val timeSlotItems = filteredSlots.map { slot ->
                        val slotTimeMinutes = getSlotTimeMinutes(slot.slotTime)
                        val isPastTime = isToday && slotTimeMinutes <= currentTimeMinutes
                        val displayTime = formatTimeDisplay(slot.slotTime)
                        val slotPrice = priceMap[displayTime] ?: 0

                        TimeSlotItem(
                            time = displayTime,
                            originalTime = slot.slotTime,
                            isAvailable = (slot.status == "AVAILABLE" || slot.status == null) && !isPastTime,
                            price = slotPrice
                        )
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        timeSlots = timeSlotItems
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
        }
    }

    // 선택된 날짜가 오늘인지 확인
    private fun isSelectedDateToday(dateString: String): Boolean {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.KOREA)
            sdf.timeZone = java.util.TimeZone.getTimeZone("Asia/Seoul")
            val selectedDate = sdf.parse(dateString)
            val today = sdf.format(java.util.Date())
            val todayDate = sdf.parse(today)
            selectedDate == todayDate
        } catch (e: Exception) {
            false
        }
    }

    // 현재 시각을 분 단위로 반환
    private fun getCurrentTimeMinutes(): Int {
        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Seoul"))
        return cal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + cal.get(java.util.Calendar.MINUTE)
    }

    // 슬롯 시간을 분 단위로 변환 (HH:mm:ss 또는 HH:mm)
    private fun getSlotTimeMinutes(slotTime: String): Int {
        return try {
            val parts = slotTime.split(":")
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()
            hour * 60 + minute
        } catch (e: Exception) {
            0
        }
    }

    // HH:mm:ss -> HH:mm 형식으로 변환
    private fun formatTimeDisplay(time: String): String {
        return try {
            val parts = time.split(":")
            if (parts.size >= 2) {
                "${parts[0]}:${parts[1]}"
            } else {
                time
            }
        } catch (e: Exception) {
            time
        }
    }

    // iOS와 동일한 셀 선택 로직
    fun onTimeSlotClick(index: Int) {
        val currentState = _uiState.value
        val timeSlots = currentState.timeSlots

        // 예약 불가능한 셀 클릭 무시
        if (index >= timeSlots.size || !timeSlots[index].isAvailable) {
            return
        }

        var selectedIndices = currentState.selectedIndices.toMutableList()
        var firstSelectedIndex = currentState.firstSelectedIndex
        var isSelectingRange = currentState.isSelectingRange

        // 첫 번째 선택
        if (firstSelectedIndex == null) {
            firstSelectedIndex = index
            selectedIndices.clear()
            selectedIndices.add(index)
            isSelectingRange = true
        }
        // 두 번째 선택 (범위 확정)
        else if (isSelectingRange) {
            val success = selectRange(firstSelectedIndex, index, timeSlots, selectedIndices)
            if (!success) {
                // 범위 내에 예약 불가 슬롯이 있음
                viewModelScope.launch {
                    _events.emit(SelectTimeEvent.ShowUnavailableAlert)
                }
                return
            }
            isSelectingRange = false
        }
        // 세 번째 이후 클릭: 전체 해제 후 새로 시작
        else {
            selectedIndices.clear()
            firstSelectedIndex = index
            selectedIndices.add(index)
            isSelectingRange = true
        }

        selectedIndices.sort()

        val updatedTimeSlots = timeSlots.mapIndexed { i, item ->
            item.copy(isSelected = selectedIndices.contains(i))
        }

        val selectedTimeRange = calculateSelectedTimeRange(selectedIndices, updatedTimeSlots, currentState.minUnit)
        val canConfirm = selectedIndices.isNotEmpty()

        // 선택한 슬롯들의 가격 합계 계산
        val totalPrice = selectedIndices.sumOf { idx -> updatedTimeSlots[idx].price }

        _uiState.value = currentState.copy(
            timeSlots = updatedTimeSlots,
            selectedIndices = selectedIndices,
            selectedTimeRange = selectedTimeRange,
            totalPrice = totalPrice,
            canConfirm = canConfirm,
            firstSelectedIndex = firstSelectedIndex,
            isSelectingRange = isSelectingRange
        )
    }

    // 범위 선택 (iOS selectRange와 동일)
    private fun selectRange(
        start: Int,
        end: Int,
        timeSlots: List<TimeSlotItem>,
        selectedIndices: MutableList<Int>
    ): Boolean {
        val range = minOf(start, end)..maxOf(start, end)

        // 범위 내에 예약 불가 셀이 있는지 확인
        for (i in range) {
            if (i >= timeSlots.size || !timeSlots[i].isAvailable) {
                return false
            }
        }

        // 범위 내 모든 셀 선택
        for (i in range) {
            if (!selectedIndices.contains(i)) {
                selectedIndices.add(i)
            }
        }

        return true
    }

    private fun calculateSelectedTimeRange(
        selectedIndices: List<Int>,
        timeSlots: List<TimeSlotItem>,
        minUnit: Int
    ): String {
        if (selectedIndices.isEmpty()) return "-"

        val sortedIndices = selectedIndices.sorted()
        val startTime = timeSlots[sortedIndices.first()].time
        val lastSlotTime = timeSlots[sortedIndices.last()].time
        val endTime = calculateEndTime(lastSlotTime, minUnit)

        // 총 시간 계산: 선택된 셀 수 × minUnit (분)
        val totalMinutes = sortedIndices.size * minUnit
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        val totalTimeText = if (minutes == 0) {
            "${hours}시간"
        } else {
            "${hours}.${minutes * 10 / 60}시간"
        }

        return "$startTime ~ $endTime (총 $totalTimeText)"
    }

    // 종료 시간 계산: 시작 시간 + minUnit
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

            String.format("%02d:%02d", endHour, endMinute)
        } catch (e: Exception) {
            startTime
        }
    }

    fun onConfirmClick() {
        val currentState = _uiState.value
        if (!currentState.canConfirm) return

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true)

            // 먼저 본인인증 상태 확인
            authRepository.checkPhoneValid()
                .onSuccess { isVerified ->
                    if (isVerified) {
                        // 본인인증 완료 - 예약 진행
                        proceedWithReservation()
                    } else {
                        // 본인인증 필요
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        _events.emit(SelectTimeEvent.RequirePhoneVerification)
                    }
                }
                .onFailure { error ->
                    val message = error.message ?: ""
                    // 401 에러 또는 인증 필요 메시지인 경우 인증 화면으로 이동
                    if (message.contains("인증이 필요") || message.contains("401")) {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        _events.emit(SelectTimeEvent.RequirePhoneVerification)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = message
                        )
                    }
                }
        }
    }

    // 본인인증 완료 후 예약 재시도
    fun retryReservationAfterVerification() {
        proceedWithReservation()
    }

    private fun proceedWithReservation() {
        val currentState = _uiState.value

        // iOS처럼 API 전송용 시간은 HH:mm 형식 (초 제외)
        val selectedTimes = currentState.selectedIndices.sorted().map { index ->
            formatTimeForApi(currentState.timeSlots[index].originalTime)
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true)

            // iOS와 동일: 시간 선택 확정 시 postReservations (room-reservations/multi) API 호출
            reservationRepository.createMultiSlotReservation(
                roomId = currentState.roomId,
                slotDate = currentState.selectedDate,
                slotTimes = selectedTimes
            )
                .onSuccess { result ->
                    _uiState.value = _uiState.value.copy(isLoading = false)

                    // 클라이언트에서 룸 가격 계산 (선택된 슬롯들의 가격 합계) - iOS와 동일
                    val roomPrice = currentState.selectedIndices.sumOf { index ->
                        currentState.timeSlots[index].price
                    }

                    _events.emit(
                        SelectTimeEvent.NavigateToReservationOption(
                            reservationId = result.reservationId,
                            roomId = currentState.roomId,
                            placeId = currentState.placeId,
                            roomName = currentState.roomName,
                            roomImageUrl = currentState.roomImageUrl,
                            selectedDate = currentState.selectedDate,
                            selectedTimes = selectedTimes,
                            minUnit = currentState.minUnit,
                            roomPrice = roomPrice
                        )
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "예약 처리 중 오류가 발생했습니다."
                    )
                }
        }
    }

    // API 전송용: HH:mm:ss -> HH:mm 변환 (iOS formatTimeForPrice와 동일)
    private fun formatTimeForApi(time: String): String {
        return try {
            val parts = time.split(":")
            if (parts.size >= 2) {
                "${parts[0]}:${parts[1]}"
            } else {
                time
            }
        } catch (e: Exception) {
            time
        }
    }

    fun onCloseClick() {
        viewModelScope.launch {
            _events.emit(SelectTimeEvent.Dismiss)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
