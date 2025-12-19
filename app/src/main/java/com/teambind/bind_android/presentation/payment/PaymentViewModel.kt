package com.teambind.bind_android.presentation.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teambind.bind_android.data.repository.PaymentRepository
import com.teambind.bind_android.data.repository.ReservationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject

data class PaymentUiState(
    val isLoading: Boolean = false,
    val reservationId: Long = 0L,
    val roomPrice: Int = 0,
    val productPrice: Int = 0,
    val totalPrice: Int = 0,
    val displayRoomPrice: String = "0원",
    val displayProductPrice: String = "0원",
    val displayTotalPrice: String = "0원",
    val displayPaymentButton: String = "총 0원 결제하기",
    val isTossPaySelected: Boolean = false,
    val consent1Agreed: Boolean = false,
    val consent2Agreed: Boolean = false,
    val consent3Agreed: Boolean = false,
    val canProceedPayment: Boolean = false,
    val errorMessage: String? = null
)

sealed class PaymentEvent {
    object PaymentSuccess : PaymentEvent()
    data class PaymentFailed(val message: String) : PaymentEvent()
    object NavigateBack : PaymentEvent()
}

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<PaymentEvent>()
    val events = _events.asSharedFlow()

    fun initialize(reservationId: Long, totalPrice: Int) {
        val formatter = NumberFormat.getNumberInstance(Locale.KOREA)
        _uiState.value = _uiState.value.copy(
            reservationId = reservationId,
            totalPrice = totalPrice,
            displayTotalPrice = "${formatter.format(totalPrice)}원",
            displayPaymentButton = "총 ${formatter.format(totalPrice)}원 결제하기"
        )
        loadReservationDetail()
    }

    private fun loadReservationDetail() {
        val currentState = _uiState.value
        viewModelScope.launch {
            reservationRepository.getMyReservationDetail(currentState.reservationId)
                .onSuccess { detail ->
                    val formatter = NumberFormat.getNumberInstance(Locale.KOREA)
                    val roomPrice = detail.reservationTimePrice
                    val productPrice = detail.totalPrice - detail.reservationTimePrice
                    val totalPrice = detail.totalPrice

                    _uiState.value = _uiState.value.copy(
                        roomPrice = roomPrice,
                        productPrice = productPrice,
                        totalPrice = totalPrice,
                        displayRoomPrice = "${formatter.format(roomPrice)}원",
                        displayProductPrice = "${formatter.format(productPrice)}원",
                        displayTotalPrice = "${formatter.format(totalPrice)}원",
                        displayPaymentButton = "총 ${formatter.format(totalPrice)}원 결제하기"
                    )
                }
                .onFailure { /* Use initial values */ }
        }
    }

    fun toggleTossPay() {
        _uiState.value = _uiState.value.copy(
            isTossPaySelected = !_uiState.value.isTossPaySelected
        )
        updatePaymentState()
    }

    fun toggleConsent1() {
        _uiState.value = _uiState.value.copy(
            consent1Agreed = !_uiState.value.consent1Agreed
        )
        updatePaymentState()
    }

    fun toggleConsent2() {
        _uiState.value = _uiState.value.copy(
            consent2Agreed = !_uiState.value.consent2Agreed
        )
        updatePaymentState()
    }

    fun toggleConsent3() {
        _uiState.value = _uiState.value.copy(
            consent3Agreed = !_uiState.value.consent3Agreed
        )
        updatePaymentState()
    }

    private fun updatePaymentState() {
        val currentState = _uiState.value
        val canProceed = currentState.isTossPaySelected &&
                currentState.consent1Agreed &&
                currentState.consent2Agreed &&
                currentState.consent3Agreed

        _uiState.value = currentState.copy(canProceedPayment = canProceed)
    }

    fun onPayClick() {
        val currentState = _uiState.value
        if (!currentState.canProceedPayment) return

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true)

            // In real implementation, paymentKey and orderId would come from Toss Payments SDK
            val paymentKey = "TOSS_${System.currentTimeMillis()}"
            val orderId = "ORDER_${currentState.reservationId}_${System.currentTimeMillis()}"

            paymentRepository.confirmPayment(
                reservationId = currentState.reservationId,
                paymentKey = paymentKey,
                orderId = orderId,
                amount = currentState.totalPrice
            )
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(PaymentEvent.PaymentSuccess)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(PaymentEvent.PaymentFailed(error.message ?: "결제에 실패했습니다."))
                }
        }
    }

    fun onBackClick() {
        viewModelScope.launch {
            _events.emit(PaymentEvent.NavigateBack)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
