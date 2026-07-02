package com.sandeep.beatx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandeep.beatx.model.Song
import com.sandeep.beatx.repository.SongRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val repository = SongRepository()
    
    private val _uiState = MutableStateFlow<UiState<List<Song>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Song>>> = _uiState.asStateFlow()

    init {
        fetchSongs()
    }

    fun fetchSongs() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val songs = repository.getTopSongs()
                if (songs.isNotEmpty()) {
                    _uiState.value = UiState.Success(songs)
                } else {
                    _uiState.value = UiState.Error("No songs found")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to load music. Please check your connection.")
            }
        }
    }
}
