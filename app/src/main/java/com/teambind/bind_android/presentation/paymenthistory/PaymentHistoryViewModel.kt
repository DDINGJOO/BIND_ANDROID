package com.teambind.bind_android.presentation.paymenthistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teambind.bind_android.data.repository.ReservationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject

data class PaymentHistoryItem(
    val paymentId: Long,
    val reservationId: Long,
    val placeName: String,
    val roomName: String,
    val paymentDate: String,
    val amount: Int,
    val displayAmount: String,
    val status: String,
    val displayStatus: String
)

data class PaymentHistoryUiState(
    val isLoading: Boolean = false,
    val payments: List<PaymentHistoryItem> = emptyList(),
    val hasNextPage: Boolean = false,
    val nextCursor: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class PaymentHistoryViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentHistoryUiState())
    val uiState: StateFlow<PaymentHistoryUiState> = _uiState.asStateFlow()

    init {
        loadPaymentHistory()
    }

    private fun loadPaymentHistory(refresh: Boolean = true) {
        val currentState = _uiState.value
        if (currentState.isLoading) return

        viewModelScope.launch {
            val cursor = if (refresh) null else currentState.nextCursor

            _uiState.value = currentState.copy(isLoading = true)

            // Use reservation data as payment history (reservations have payment info)
            reservationRepository.getMyReservations(cursor = cursor)
                .onSuccess { response ->
                    val formatter = NumberFormat.getNumberInstance(Locale.KOREA)
                    val payments = response.content.map { reservation ->
                        PaymentHistoryItem(
                            paymentId = reservation.reservationId,
                            reservationId = reservation.reservationId,
                            placeName = reservation.placeName ?: "",
                            roomName = reservation.roomName ?: "",
                            paymentDate = reservation.reservationDate.split("T").firstOrNull()
                                ?: reservation.reservationDate,
                            amount = reservation.totalPrice,
                            displayAmount = "${formatter.format(reservation.totalPrice)}원",
                            status = reservation.status,
                            displayStatus = when (reservation.status) {
                                "APPROVED", "CONFIRMED" -> "결제 완료"
                                "CANCELLED" -> "취소됨"
                                "PENDING" -> "결제 대기"
                                else -> reservation.status
                            }
                        )
                    }

                    val allPayments = if (refresh) payments else currentState.payments + payments

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        payments = allPayments,
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

    fun loadMore() {
        val currentState = _uiState.value
        if (currentState.hasNextPage && !currentState.isLoading) {
            loadPaymentHistory(refresh = false)
        }
    }

    fun refresh() {
        loadPaymentHistory(refresh = true)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
