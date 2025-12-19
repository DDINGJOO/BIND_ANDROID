package com.teambind.bind_android.presentation.reservationform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teambind.bind_android.data.local.PreferencesManager
import com.teambind.bind_android.data.model.response.ReservationFieldDto
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

data class ReservationFormUiState(
    val isLoading: Boolean = false,
    val reservationId: Long = 0L,
    val roomId: Long = 0L,
    val roomName: String = "",
    val roomImageUrl: String? = null,
    val selectedDate: String = "",
    val displayDate: String = "",
    val selectedTimes: List<String> = emptyList(),
    val displayTimeRange: String = "",
    val roomPrice: Int = 0,
    val optionPrice: Int = 0,
    val totalPrice: Int = 0,
    val displayRoomPrice: String = "0원",
    val displayOptionPrice: String = "0원",
    val displayTotalPrice: String = "0원 결제하기",
    val dynamicFields: List<ReservationFieldDto> = emptyList(),
    val name: String = "",
    val contact: String = "",
    val additionalFieldValues: Map<String, String> = emptyMap(),
    val isTermsServiceAgreed: Boolean = false,
    val isPrivacyPolicyAgreed: Boolean = false,
    val isThirdPartyAgreed: Boolean = false,
    val isCancellationPolicyAgreed: Boolean = false,
    val isAllTermsAgreed: Boolean = false,
    val isFormValid: Boolean = false,
    val errorMessage: String? = null
)

sealed class ReservationFormEvent {
    data class NavigateToReservationCheck(
        val reservationId: Long,
        val roomName: String,
        val roomImageUrl: String?,
        val selectedDate: String,
        val selectedTimes: List<String>,
        val totalPrice: Int
    ) : ReservationFormEvent()
    object NavigateBack : ReservationFormEvent()
    data class ShowError(val message: String) : ReservationFormEvent()
}

@HiltViewModel
class ReservationFormViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReservationFormUiState())
    val uiState: StateFlow<ReservationFormUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ReservationFormEvent>()
    val events = _events.asSharedFlow()

    fun initialize(
        reservationId: Long,
        roomId: Long,
        roomName: String,
        roomImageUrl: String?,
        selectedDate: String,
        selectedTimes: List<String>,
        roomPrice: Int,
        optionPrice: Int,
        totalPrice: Int
    ) {
        val formatter = NumberFormat.getNumberInstance(Locale.KOREA)
        _uiState.value = _uiState.value.copy(
            reservationId = reservationId,
            roomId = roomId,
            roomName = roomName,
            roomImageUrl = roomImageUrl,
            selectedDate = selectedDate,
            selectedTimes = selectedTimes,
            displayDate = formatDisplayDate(selectedDate),
            displayTimeRange = formatTimeRange(selectedTimes),
            roomPrice = roomPrice,
            optionPrice = optionPrice,
            totalPrice = totalPrice,
            displayRoomPrice = "${formatter.format(roomPrice)}원",
            displayOptionPrice = "${formatter.format(optionPrice)}원",
            displayTotalPrice = "${formatter.format(totalPrice)}원 결제하기"
        )
        confirmReservation()
        loadReservationFields()
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
        return "$startTime ~ $endTime"
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

    private fun confirmReservation() {
        val currentState = _uiState.value
        viewModelScope.launch {
            reservationRepository.confirmReservation(currentState.reservationId)
                .onSuccess { result ->
                    val formatter = NumberFormat.getNumberInstance(Locale.KOREA)
                    _uiState.value = _uiState.value.copy(
                        totalPrice = result.totalPrice,
                        displayTotalPrice = "${formatter.format(result.totalPrice)}원 결제하기"
                    )
                }
                .onFailure { error ->
                    viewModelScope.launch {
                        _events.emit(ReservationFormEvent.ShowError("예약 확정에 실패했습니다."))
                    }
                }
        }
    }

    private fun loadReservationFields() {
        val currentState = _uiState.value
        viewModelScope.launch {
            reservationRepository.getReservationFields(currentState.roomId)
                .onSuccess { fields ->
                    val sortedFields = fields.sortedBy { it.sequence }
                    _uiState.value = _uiState.value.copy(dynamicFields = sortedFields)
                }
                .onFailure { /* Ignore - fields are optional */ }
        }
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
        validateForm()
    }

    fun updateContact(contact: String) {
        _uiState.value = _uiState.value.copy(contact = contact)
        validateForm()
    }

    fun updateAdditionalField(fieldTitle: String, value: String) {
        val updatedValues = _uiState.value.additionalFieldValues.toMutableMap()
        updatedValues[fieldTitle] = value
        _uiState.value = _uiState.value.copy(additionalFieldValues = updatedValues)
        validateForm()
    }

    private fun validateForm() {
        val currentState = _uiState.value
        val isNameValid = currentState.name.isNotBlank()
        val isContactValid = currentState.contact.isNotBlank()

        val requiredFieldsFilled = currentState.dynamicFields
            .filter { it.required }
            .all { field ->
                currentState.additionalFieldValues[field.title]?.isNotBlank() == true
            }

        val isAllTermsAgreed = currentState.isTermsServiceAgreed &&
                currentState.isPrivacyPolicyAgreed &&
                currentState.isThirdPartyAgreed &&
                currentState.isCancellationPolicyAgreed

        _uiState.value = currentState.copy(
            isAllTermsAgreed = isAllTermsAgreed,
            isFormValid = isNameValid && isContactValid && requiredFieldsFilled && isAllTermsAgreed
        )
    }

    fun toggleAllTerms() {
        val currentState = _uiState.value
        val newValue = !currentState.isAllTermsAgreed
        _uiState.value = currentState.copy(
            isTermsServiceAgreed = newValue,
            isPrivacyPolicyAgreed = newValue,
            isThirdPartyAgreed = newValue,
            isCancellationPolicyAgreed = newValue
        )
        validateForm()
    }

    fun toggleTermsService() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(isTermsServiceAgreed = !currentState.isTermsServiceAgreed)
        validateForm()
    }

    fun togglePrivacyPolicy() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(isPrivacyPolicyAgreed = !currentState.isPrivacyPolicyAgreed)
        validateForm()
    }

    fun toggleThirdParty() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(isThirdPartyAgreed = !currentState.isThirdPartyAgreed)
        validateForm()
    }

    fun toggleCancellationPolicy() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(isCancellationPolicyAgreed = !currentState.isCancellationPolicyAgreed)
        validateForm()
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
        val numericOnly = phone.filter { it.isDigit() }
        return phone == numericOnly && phone.startsWith("010") && phone.length == 11
    }

    fun onConfirmClick() {
        val currentState = _uiState.value

        if (!isValidPhoneNumber(currentState.contact)) {
            viewModelScope.launch {
                _events.emit(ReservationFormEvent.ShowError("연락처는 010으로 시작하는 11자리 숫자여야 합니다."))
            }
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true)

            // Get userId from PreferencesManager (iOS와 동일)
            val userId = preferencesManager.getUserId()
            if (userId == null) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                _events.emit(ReservationFormEvent.ShowError("로그인이 필요합니다."))
                return@launch
            }

            reservationRepository.updateReservationUserInfo(
                reservationId = currentState.reservationId,
                userId = userId,
                reserverName = currentState.name,
                reserverPhone = currentState.contact,
                additionalInfo = currentState.additionalFieldValues.takeIf { it.isNotEmpty() }
            )
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(
                        ReservationFormEvent.NavigateToReservationCheck(
                            reservationId = currentState.reservationId,
                            roomName = currentState.roomName,
                            roomImageUrl = currentState.roomImageUrl,
                            selectedDate = currentState.selectedDate,
                            selectedTimes = currentState.selectedTimes,
                            totalPrice = currentState.totalPrice
                        )
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(ReservationFormEvent.ShowError("예약자 정보 업데이트에 실패했습니다."))
                }
        }
    }

    fun onBackClick() {
        viewModelScope.launch {
            _events.emit(ReservationFormEvent.NavigateBack)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
