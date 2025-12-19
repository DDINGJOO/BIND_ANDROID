package com.teambind.bind_android.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teambind.bind_android.data.model.response.PlaceDto
import com.teambind.bind_android.data.repository.PlaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val isLoading: Boolean = false,
    val query: String = "",
    val recentSearches: List<String> = emptyList(),
    val searchResults: List<PlaceDto> = emptyList(),
    val isSearching: Boolean = false,
    val showNoResults: Boolean = false,
    val errorMessage: String? = null
)

sealed class SearchEvent {
    data class NavigateToDetail(val placeId: Long) : SearchEvent()
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val placeRepository: PlaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SearchEvent>()
    val events = _events.asSharedFlow()

    init {
        loadRecentSearches()
    }

    private fun loadRecentSearches() {
        // Load from SharedPreferences or local storage
        // For now, using empty list
        _uiState.value = _uiState.value.copy(recentSearches = emptyList())
    }

    fun updateQuery(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
    }

    fun search() {
        val query = _uiState.value.query.trim()
        if (query.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isSearching = true)

            placeRepository.searchPlaces(query)
                .onSuccess { places ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        searchResults = places,
                        showNoResults = places.isEmpty()
                    )
                    saveRecentSearch(query)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
        }
    }

    private fun saveRecentSearch(query: String) {
        val currentSearches = _uiState.value.recentSearches.toMutableList()
        currentSearches.remove(query)
        currentSearches.add(0, query)
        if (currentSearches.size > 10) {
            currentSearches.removeLast()
        }
        _uiState.value = _uiState.value.copy(recentSearches = currentSearches)
    }

    fun onRecentSearchClick(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        search()
    }

    fun removeRecentSearch(query: String) {
        val currentSearches = _uiState.value.recentSearches.toMutableList()
        currentSearches.remove(query)
        _uiState.value = _uiState.value.copy(recentSearches = currentSearches)
    }

    fun clearAllRecentSearches() {
        _uiState.value = _uiState.value.copy(recentSearches = emptyList())
    }

    fun onPlaceClick(placeId: Long) {
        viewModelScope.launch {
            _events.emit(SearchEvent.NavigateToDetail(placeId))
        }
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(
            query = "",
            isSearching = false,
            searchResults = emptyList(),
            showNoResults = false
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
