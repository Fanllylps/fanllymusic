package com.alfan.fanllymusic.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alfan.fanllymusic.data.local.entity.MetadataOverrideEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MetadataOverrideDao {
    @Query("SELECT * FROM metadata_overrides")
    fun observeOverrides(): Flow<List<MetadataOverrideEntity>>

    @Query("SELECT * FROM metadata_overrides WHERE songKey = :songKey")
    suspend fun getOverride(songKey: String): MetadataOverrideEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(override: MetadataOverrideEntity)

    @Query("DELETE FROM metadata_overrides WHERE songKey = :songKey")
    suspend fun delete(songKey: String)
}
