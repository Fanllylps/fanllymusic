package com.alfan.fanllymusic.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alfan.fanllymusic.data.local.entity.LyricsCacheEntity
import com.alfan.fanllymusic.data.local.entity.LyricsEntity

@Dao
interface LyricsDao {
    @Query("SELECT * FROM lyrics WHERE songId = :songId")
    suspend fun getLyrics(songId: Long): LyricsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(lyrics: LyricsEntity)

    @Query("SELECT * FROM lyrics_cache WHERE songKey = :songKey")
    suspend fun getCachedLyrics(songKey: String): LyricsCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCache(lyrics: LyricsCacheEntity)

    @Query("DELETE FROM lyrics")
    suspend fun clear()
}
