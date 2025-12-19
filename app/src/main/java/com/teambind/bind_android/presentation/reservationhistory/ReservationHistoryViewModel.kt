package com.teambind.bind_android.presentation.reservationhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teambind.bind_android.data.model.response.MyReservationDto
import com.teambind.bind_android.data.repository.ReservationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ReservationTab(val displayName: String, val status: String?) {
    ALL("전체", null),
    UPCOMING("예정된 예약", "CONFIRMED"),
    COMPLETED("이용 완료", "COMPLETED")
}

data class ReservationHistoryUiState(
    val isLoading: Boolean = false,
    val selectedTab: ReservationTab = ReservationTab.ALL,
    val reservations: List<MyReservationDto> = emptyList(),
    val hasNextPage: Boolean = false,
    val nextCursor: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ReservationHistoryViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReservationHistoryUiState())
    val uiState: StateFlow<ReservationHistoryUiState> = _uiState.asStateFlow()

    init {
        loadReservations()
    }

    fun selectTab(tab: ReservationTab) {
        if (_uiState.value.selectedTab != tab) {
            _uiState.value = _uiState.value.copy(
                selectedTab = tab,
                reservations = emptyList(),
                nextCursor = null
            )
            loadReservations()
        }
    }

    fun loadReservations(refresh: Boolean = true) {
        val currentState = _uiState.value
        if (currentState.isLoading) return

        viewModelScope.launch {
            val cursor = if (refresh) null else currentState.nextCursor

            _uiState.value = currentState.copy(isLoading = true)

            reservationRepository.getMyReservations(
                status = currentState.selectedTab.status,
                cursor = cursor
            )
                .onSuccess { response ->
                    val newReservations = response.content
                    val allReservations = if (refresh) {
                        newReservations
                    } else {
                        currentState.reservations + newReservations
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        reservations = allReservations,
                        hasNextPage = response.cursor?.hasNext ?: false,
                        nextCursor = response.cursor?.next
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

    fun loadMoreReservations() {
        val currentState = _uiState.value
        if (currentState.hasNextPage && !currentState.isLoading) {
            loadReservations(refresh = false)
        }
    }

    fun refresh() {
        loadReservations(refresh = true)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
