package com.sandeep.beatx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandeep.beatx.model.Song
import com.sandeep.beatx.repository.SearchRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    private val repository = SearchRepository()
    
    private val _uiState = MutableStateFlow<UiState<List<Song>>>(UiState.Success(emptyList()))
    val uiState: StateFlow<UiState<List<Song>>> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    var lastQuery: String = ""

    fun setQuery(query: String) {
        lastQuery = query
        searchJob?.cancel()
        
        if (query.isBlank()) {
            _uiState.value = UiState.Success(emptyList())
            return
        }

        searchJob = viewModelScope.launch {
            delay(500) // Debounce
            _uiState.value = UiState.Loading
            try {
                val songs = repository.searchSongs(query)
                if (songs.isNotEmpty()) {
                    _uiState.value = UiState.Success(songs)
                } else {
                    _uiState.value = UiState.Error("No songs found for '$query'")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to search. Please check your connection.")
            }
        }
    }
}
