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

enum class FindViewMode {
    MAP, LIST
}

data class FindUiState(
    val isLoading: Boolean = false,
    val currentAddress: String = "서울 강남역",
    val studios: List<StudioDto> = emptyList(),
    val selectedStudio: StudioDto? = null,
    val errorMessage: String? = null,
    // 현재 지도 중심 좌표 (강남역)
    val centerLatitude: Double = 37.4979,
    val centerLongitude: Double = 127.0276,
    // 뷰 모드 (지도/목록)
    val viewMode: FindViewMode = FindViewMode.MAP
)

@HiltViewModel
class FindViewModel @Inject constructor(
    private val studioRepository: StudioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FindUiState())
    val uiState: StateFlow<FindUiState> = _uiState.asStateFlow()

    // 초기 로드는 Fragment에서 위치를 받은 후 onMapCameraIdle()로 호출됨

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

    fun onMapCameraIdle(latitude: Double, longitude: Double) {
        // 카메라 이동이 완료되면 해당 위치 기준으로 스튜디오 검색
        updateMapCenter(latitude, longitude)
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

    fun toggleViewMode() {
        val newMode = if (_uiState.value.viewMode == FindViewMode.MAP) {
            FindViewMode.LIST
        } else {
            FindViewMode.MAP
        }
        _uiState.value = _uiState.value.copy(viewMode = newMode)
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
