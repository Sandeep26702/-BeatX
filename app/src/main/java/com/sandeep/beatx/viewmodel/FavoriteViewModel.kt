package com.sandeep.beatx.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sandeep.beatx.BeatXApplication
import com.sandeep.beatx.model.Song
import com.sandeep.beatx.repository.FavoriteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class FavoriteViewModel(application: Application) : AndroidViewModel(application) {
    private val database = (application as BeatXApplication).database
    private val repository = FavoriteRepository(database.favoriteSongDao())

    val favorites: StateFlow<List<Song>> = repository.getAllFavorites()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun checkIsFavorite(songId: String): Flow<Boolean> {
        return repository.isFavorite(songId)
    }

    fun toggleFavorite(song: Song, isCurrentlyFavorite: Boolean) {
        viewModelScope.launch {
            if (isCurrentlyFavorite) {
                repository.removeFavorite(song)
            } else {
                repository.addFavorite(song)
            }
        }
    }
}
