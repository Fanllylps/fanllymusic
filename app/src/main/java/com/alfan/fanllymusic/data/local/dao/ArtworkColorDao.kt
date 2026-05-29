package com.alfan.fanllymusic.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alfan.fanllymusic.data.local.entity.ArtworkColorEntity

@Dao
interface ArtworkColorDao {
    @Query("SELECT * FROM artwork_colors WHERE songKey = :songKey")
    suspend fun getColor(songKey: String): ArtworkColorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(color: ArtworkColorEntity)
}
