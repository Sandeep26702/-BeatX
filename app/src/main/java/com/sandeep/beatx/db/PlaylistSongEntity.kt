package com.sandeep.beatx.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.sandeep.beatx.model.Song

@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "songId"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["playlistId"])]
)
data class PlaylistSongEntity(
    val playlistId: Int,
    val songId: String,
    val title: String,
    val artist: String,
    val imageUrl: String,
    val audioUrl: String,
    val addedAt: Long = System.currentTimeMillis()
) {
    fun toSong(): Song {
        return Song(
            id = songId,
            title = title,
            artist = artist,
            album = "",
            imageUrl = imageUrl,
            audioUrl = audioUrl,
            duration = ""
        )
    }
}
