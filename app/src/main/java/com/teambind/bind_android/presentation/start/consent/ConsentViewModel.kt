package com.teambind.bind_android.presentation.start.consent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teambind.bind_android.data.repository.SignUpRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConsentState(
    val serviceConsent: Boolean = false,
    val privacyConsent: Boolean = false,
    val marketingConsent: Boolean = false
) {
    val isAllAgreed: Boolean
        get() = serviceConsent && privacyConsent && marketingConsent

    val isRequiredAgreed: Boolean
        get() = serviceConsent && privacyConsent
}

@HiltViewModel
class ConsentViewModel @Inject constructor(
    private val signUpRepository: SignUpRepository
) : ViewModel() {

    private var email: String = ""
    private var password: String = ""

    private val _consentState = MutableStateFlow(ConsentState())
    val consentState: StateFlow<ConsentState> = _consentState.asStateFlow()

    private val _signUpResult = MutableStateFlow<Result<Unit>?>(null)
    val signUpResult: StateFlow<Result<Unit>?> = _signUpResult.asStateFlow()

    fun setCredentials(email: String, password: String) {
        this.email = email
        this.password = password
    }

    fun toggleAllAgree() {
        val newValue = !_consentState.value.isAllAgreed
        _consentState.value = ConsentState(
            serviceConsent = newValue,
            privacyConsent = newValue,
            marketingConsent = newValue
        )
    }

    fun toggleServiceConsent() {
        _consentState.value = _consentState.value.copy(
            serviceConsent = !_consentState.value.serviceConsent
        )
    }

    fun togglePrivacyConsent() {
        _consentState.value = _consentState.value.copy(
            privacyConsent = !_consentState.value.privacyConsent
        )
    }

    fun toggleMarketingConsent() {
        _consentState.value = _consentState.value.copy(
            marketingConsent = !_consentState.value.marketingConsent
        )
    }

    fun signUp() {
        viewModelScope.launch {
            val state = _consentState.value
            val consents = listOf(
                state.serviceConsent,
                state.privacyConsent,
                state.marketingConsent
            )

            signUpRepository.signUp(
                email = email,
                password = password,
                passwordConfirm = password,
                consents = consents
            ).also { result ->
                _signUpResult.value = result
            }
        }
    }
}
