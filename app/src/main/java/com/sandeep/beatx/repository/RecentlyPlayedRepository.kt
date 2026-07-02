package com.sandeep.beatx.repository

import com.sandeep.beatx.db.RecentlyPlayedDao
import com.sandeep.beatx.db.RecentlyPlayedEntity
import com.sandeep.beatx.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RecentlyPlayedRepository(private val dao: RecentlyPlayedDao) {

    fun getRecentlyPlayed(): Flow<List<Song>> {
        return dao.getRecentlyPlayed().map { entities ->
            entities.map { it.toSong() }
        }
    }

    suspend fun addRecentlyPlayed(song: Song) {
        dao.insert(RecentlyPlayedEntity.fromSong(song))
    }
}
