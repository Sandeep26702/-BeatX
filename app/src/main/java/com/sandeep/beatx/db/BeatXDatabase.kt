package com.sandeep.beatx.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [
    FavoriteSongEntity::class, 
    RecentlyPlayedEntity::class,
    PlaylistEntity::class,
    PlaylistSongEntity::class
], version = 3, exportSchema = false)
abstract class BeatXDatabase : RoomDatabase() {
    abstract fun favoriteSongDao(): FavoriteSongDao
    abstract fun recentlyPlayedDao(): RecentlyPlayedDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        @Volatile
        private var INSTANCE: BeatXDatabase? = null

        fun getDatabase(context: Context): BeatXDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BeatXDatabase::class.java,
                    "beatx_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
