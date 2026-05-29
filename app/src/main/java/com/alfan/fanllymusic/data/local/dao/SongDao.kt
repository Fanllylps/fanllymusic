package com.alfan.fanllymusic.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.alfan.fanllymusic.data.local.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY title COLLATE NOCASE ASC")
    fun observeSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getSong(id: Long): SongEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(songs: List<SongEntity>)

    @Transaction
    suspend fun replaceAll(songs: List<SongEntity>) {
        clear()
        upsertAll(songs)
    }

    @Query("UPDATE songs SET isFavorite = :favorite WHERE id = :songId")
    suspend fun setFavorite(songId: Long, favorite: Boolean)

    @Query("DELETE FROM songs")
    suspend fun clear()
}
