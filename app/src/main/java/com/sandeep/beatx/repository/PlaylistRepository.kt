package com.sandeep.beatx.repository

import com.sandeep.beatx.db.PlaylistDao
import com.sandeep.beatx.db.PlaylistEntity
import com.sandeep.beatx.db.PlaylistSongEntity
import com.sandeep.beatx.db.PlaylistWithSongs
import com.sandeep.beatx.model.Song
import kotlinx.coroutines.flow.Flow

class PlaylistRepository(private val playlistDao: PlaylistDao) {

    val allPlaylists: Flow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()

    suspend fun createPlaylist(name: String): Long {
        return playlistDao.insertPlaylist(PlaylistEntity(name = name))
    }

    suspend fun renamePlaylist(playlistId: Int, newName: String) {
        playlistDao.updatePlaylist(PlaylistEntity(id = playlistId, name = newName))
    }

    suspend fun deletePlaylist(playlistId: Int) {
        playlistDao.deletePlaylist(playlistId)
    }

    suspend fun addSongToPlaylist(playlistId: Int, song: Song) {
        val entity = PlaylistSongEntity(
            playlistId = playlistId,
            songId = song.id,
            title = song.title,
            artist = song.artist,
            imageUrl = song.imageUrl,
            audioUrl = song.audioUrl
        )
        playlistDao.addSongToPlaylist(entity)
    }

    suspend fun removeSongFromPlaylist(playlistId: Int, songId: String) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }

    fun getPlaylistWithSongs(playlistId: Int): Flow<PlaylistWithSongs> {
        return playlistDao.getPlaylistWithSongs(playlistId)
    }
}
