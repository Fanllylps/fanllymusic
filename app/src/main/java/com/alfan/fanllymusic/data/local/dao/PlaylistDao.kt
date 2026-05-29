package com.alfan.fanllymusic.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alfan.fanllymusic.data.local.entity.PlaylistEntity
import com.alfan.fanllymusic.data.local.entity.PlaylistSongCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun observePlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(playlist: PlaylistEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSong(crossRef: PlaylistSongCrossRef)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun delete(playlistId: Long)
}
