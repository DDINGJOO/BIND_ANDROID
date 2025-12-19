package com.teambind.bind_android.presentation.studiodetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teambind.bind_android.data.model.response.*
import com.teambind.bind_android.data.repository.StudioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudioDetailUiState(
    val isLoading: Boolean = false,
    val room: RoomDetailDto? = null,
    val place: PlaceDetailDto? = null,
    val roomDetails: List<RoomDetailResponse> = emptyList(),  // Place의 Room 상세 목록 (가격 포함)
    val pricingPolicy: PricingPolicyDto? = null,
    val products: List<ProductDto> = emptyList(),
    val isBookmarked: Boolean = false,
    val errorMessage: String? = null,
    val loadFailed: Boolean = false,
    val roomLoadFailed: Boolean = false,  // Room API 실패 여부 (place는 성공)
    val pricingLoadFailed: Boolean = false  // 가격 정보 조회 실패 (iOS처럼 graceful degradation)
)

@HiltViewModel
class StudioDetailViewModel @Inject constructor(
    private val studioRepository: StudioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudioDetailUiState())
    val uiState: StateFlow<StudioDetailUiState> = _uiState.asStateFlow()

    private var roomId: Long = 0
    private var placeId: String = ""

    // Place ID로 상세 조회 (iOS 방식: 공간 정보 먼저 표시, Room은 선택적)
    fun loadPlaceDetail(placeId: String) {
        this.placeId = placeId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                studioRepository.getPlaceDetail(placeId)
                    .onSuccess { place ->
                        // Place 정보 먼저 저장 (iOS처럼)
                        _uiState.update {
                            it.copy(place = place)
                        }

                        // Room ID가 있으면 각 Room의 상세 정보 조회
                        val roomIds = place.roomIds ?: emptyList()
                        if (roomIds.isNotEmpty()) {
                            // 각 roomId에 대해 /rooms/{roomId} API 호출
                            loadRoomDetailsList(roomIds)
                        }

                        _uiState.update {
                            it.copy(isLoading = false)
                        }
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = error.message ?: "공간 정보를 불러오는데 실패했습니다.",
                                loadFailed = true
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "공간 정보를 불러오는데 실패했습니다.",
                        loadFailed = true
                    )
                }
            }
        }
    }

    // Room 상세 목록 조회 (각 roomId에 대해 /rooms/{roomId} 호출)
    private suspend fun loadRoomDetailsList(roomIds: List<Long>) {
        if (roomIds.isEmpty()) {
            _uiState.update { it.copy(roomDetails = emptyList()) }
            return
        }

        try {
            // 각 roomId에 대해 병렬로 상세 정보 조회
            val deferredResults = roomIds.map { roomId ->
                viewModelScope.async {
                    try {
                        studioRepository.getRoomDetail(roomId).getOrNull()
                    } catch (e: Exception) {
                        null
                    }
                }
            }

            // 모든 결과 대기 후 성공한 것만 필터링
            val results = deferredResults.awaitAll()
            val successfulRoomDetails = results.filterNotNull().filter { it.room != null }

            _uiState.update {
                it.copy(roomDetails = successfulRoomDetails)
            }
        } catch (e: Exception) {
            // 예외 발생해도 무시
            _uiState.update { it.copy(roomDetails = emptyList()) }
        }
    }

    // Room 조회 시도, 실패해도 Place 정보는 유지 (iOS graceful degradation)
    private suspend fun loadRoomDetailWithPlaceFallback(roomId: Long) {
        this.roomId = roomId
        try {
            studioRepository.getRoomDetail(roomId)
                .onSuccess { response ->
                    if (response.room != null) {
                        // 가격 정보가 없어도 Room은 표시 (iOS처럼 graceful degradation)
                        val pricingLoadFailed = response.pricingPolicy == null
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                room = response.room,
                                place = response.place ?: it.place,  // 기존 place 유지
                                pricingPolicy = response.pricingPolicy,
                                products = response.availableProducts ?: emptyList(),
                                pricingLoadFailed = pricingLoadFailed
                            )
                        }
                    } else {
                        // Room 데이터 없음 - place 정보만 표시
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                roomLoadFailed = true
                            )
                        }
                    }
                }
                .onFailure {
                    // Room API 실패 - place 정보는 유지하고 표시
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            roomLoadFailed = true
                        )
                    }
                }
        } catch (e: Exception) {
            // Room API 예외 - place 정보는 유지
            _uiState.update {
                it.copy(
                    isLoading = false,
                    roomLoadFailed = true
                )
            }
        }
    }

    // Room ID로 상세 조회
    fun loadRoomDetail(roomId: Long) {
        this.roomId = roomId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                studioRepository.getRoomDetail(roomId)
                    .onSuccess { response ->
                        if (response.room == null || response.place == null) {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = "연습실 정보를 불러오는데 실패했습니다.",
                                    loadFailed = true
                                )
                            }
                            return@onSuccess
                        }
                        // 가격 정보가 없어도 Room은 표시 (iOS처럼 graceful degradation)
                        val pricingLoadFailed = response.pricingPolicy == null
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                room = response.room,
                                place = response.place,
                                pricingPolicy = response.pricingPolicy,
                                products = response.availableProducts ?: emptyList(),
                                pricingLoadFailed = pricingLoadFailed
                            )
                        }

                        // Place의 Room 상세 목록도 로드
                        val roomIds = response.place.roomIds ?: emptyList()
                        if (roomIds.isNotEmpty()) {
                            loadRoomDetailsList(roomIds)
                        }
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = error.message ?: "연습실 정보를 불러오는데 실패했습니다.",
                                loadFailed = true
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "연습실 정보를 불러오는데 실패했습니다.",
                        loadFailed = true
                    )
                }
            }
        }
    }

    fun toggleBookmark() {
        _uiState.update { it.copy(isBookmarked = !it.isBookmarked) }
        // TODO: API 호출하여 북마크 상태 저장
    }

    // Room 선택 시 해당 Room 상세 정보 로드
    fun selectRoom(roomId: Long) {
        this.roomId = roomId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            loadRoomDetailWithPlaceFallback(roomId)
        }
    }

    // 이미 받은 RoomDetailResponse 데이터를 직접 설정 (재조회 불필요)
    fun setRoomDetailDirectly(roomDetail: RoomDetailResponse) {
        viewModelScope.launch {
            val pricingLoadFailed = roomDetail.pricingPolicy == null

            _uiState.update {
                it.copy(
                    isLoading = false,
                    room = roomDetail.room,
                    place = roomDetail.place,
                    pricingPolicy = roomDetail.pricingPolicy,
                    products = roomDetail.availableProducts ?: emptyList(),
                    pricingLoadFailed = pricingLoadFailed
                )
            }

            // Place의 Room 상세 목록도 로드
            roomDetail.place?.roomIds?.let { roomIds ->
                if (roomIds.isNotEmpty()) {
                    loadRoomDetailsList(roomIds)
                }
            }
        }
    }

    // 현재 roomId 반환
    fun getCurrentRoomId(): Long = roomId

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
