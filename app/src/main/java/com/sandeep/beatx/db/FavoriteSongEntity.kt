package com.sandeep.beatx.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sandeep.beatx.model.Song

@Entity(tableName = "favorite_songs")
data class FavoriteSongEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val imageUrl: String,
    val audioUrl: String,
    val duration: String
) {
    fun toSong(): Song = Song(
        id = id,
        title = title,
        artist = artist,
        album = album,
        imageUrl = imageUrl,
        audioUrl = audioUrl,
        duration = duration
    )

    companion object {
        fun fromSong(song: Song): FavoriteSongEntity = FavoriteSongEntity(
            id = song.id,
            title = song.title,
            artist = song.artist,
            album = song.album,
            imageUrl = song.imageUrl,
            audioUrl = song.audioUrl,
            duration = song.duration
        )
    }
}
