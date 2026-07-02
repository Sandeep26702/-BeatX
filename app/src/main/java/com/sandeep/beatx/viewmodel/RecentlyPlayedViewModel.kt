package com.sandeep.beatx.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sandeep.beatx.BeatXApplication
import com.sandeep.beatx.model.Song
import com.sandeep.beatx.repository.RecentlyPlayedRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecentlyPlayedViewModel(application: Application) : AndroidViewModel(application) {
    private val database = (application as BeatXApplication).database
    private val repository = RecentlyPlayedRepository(database.recentlyPlayedDao())

    val recentlyPlayed: StateFlow<List<Song>> = repository.getRecentlyPlayed()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addSong(song: Song) {
        viewModelScope.launch {
            repository.addRecentlyPlayed(song)
        }
    }
}
