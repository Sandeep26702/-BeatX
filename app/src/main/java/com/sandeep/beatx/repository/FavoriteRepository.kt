package com.sandeep.beatx.repository

import com.sandeep.beatx.db.FavoriteSongDao
import com.sandeep.beatx.db.FavoriteSongEntity
import com.sandeep.beatx.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoriteRepository(private val dao: FavoriteSongDao) {
    
    fun getAllFavorites(): Flow<List<Song>> {
        return dao.getAllFavorites().map { entities -> 
            entities.map { it.toSong() }
        }
    }

    fun isFavorite(songId: String): Flow<Boolean> {
        return dao.isFavorite(songId)
    }

    suspend fun addFavorite(song: Song) {
        dao.insert(FavoriteSongEntity.fromSong(song))
    }

    suspend fun removeFavorite(song: Song) {
        dao.delete(FavoriteSongEntity.fromSong(song))
    }
}
