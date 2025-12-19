package com.teambind.bind_android.presentation.find

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teambind.bind_android.data.model.response.StudioDto
import com.teambind.bind_android.data.repository.StudioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FindUiState(
    val isLoading: Boolean = false,
    val currentAddress: String = "인천광역시 구월동",
    val studios: List<StudioDto> = emptyList(),
    val selectedStudio: StudioDto? = null,
    val errorMessage: String? = null,
    // 현재 지도 중심 좌표
    val centerLatitude: Double = 37.4480,
    val centerLongitude: Double = 126.7025
)

@HiltViewModel
class FindViewModel @Inject constructor(
    private val studioRepository: StudioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FindUiState())
    val uiState: StateFlow<FindUiState> = _uiState.asStateFlow()

    init {
        loadNearbyStudios()
    }

    fun loadNearbyStudios() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val latitude = _uiState.value.centerLatitude
            val longitude = _uiState.value.centerLongitude

            studioRepository.getNearbyStudios(latitude, longitude, radius = 5000)
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        studios = response.studioList ?: emptyList()
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

    fun updateMapCenter(latitude: Double, longitude: Double) {
        _uiState.value = _uiState.value.copy(
            centerLatitude = latitude,
            centerLongitude = longitude
        )
        loadNearbyStudios()
    }

    fun selectStudio(studio: StudioDto) {
        _uiState.value = _uiState.value.copy(selectedStudio = studio)
    }

    fun clearSelectedStudio() {
        _uiState.value = _uiState.value.copy(selectedStudio = null)
    }

    fun updateAddress(address: String) {
        _uiState.value = _uiState.value.copy(currentAddress = address)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun searchStudios(keyword: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            studioRepository.searchStudios(keyword)
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        studios = response.studioList ?: emptyList()
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
}
