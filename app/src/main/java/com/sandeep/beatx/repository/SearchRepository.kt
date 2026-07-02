package com.sandeep.beatx.repository

import com.sandeep.beatx.api.RetrofitClient
import com.sandeep.beatx.model.Song
import com.sandeep.beatx.model.TrackDto

class SearchRepository {
    private val api = RetrofitClient.apiService

    suspend fun searchSongs(query: String): List<Song> {
        val response = api.searchTracks(query)
        return response.data.map { mapToDomain(it) }
    }

    private fun mapToDomain(dto: TrackDto): Song {
        val minutes = dto.duration / 60
        val seconds = dto.duration % 60
        val durationString = String.format("%d:%02d", minutes, seconds)

        return Song(
            id = dto.id.toString(),
            title = dto.title,
            artist = dto.artist.name,
            album = dto.album.title,
            imageUrl = dto.album.coverXl ?: dto.album.coverMedium,
            audioUrl = dto.preview,
            duration = durationString
        )
    }
}
