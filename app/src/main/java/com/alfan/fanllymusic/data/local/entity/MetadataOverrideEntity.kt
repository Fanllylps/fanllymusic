package com.alfan.fanllymusic.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "metadata_overrides")
data class MetadataOverrideEntity(
    @PrimaryKey val songKey: String,
    val title: String?,
    val artist: String?,
    val album: String?,
    val genre: String?,
    val year: Int?,
    val trackNumber: Int?,
    val updatedAt: Long
)
