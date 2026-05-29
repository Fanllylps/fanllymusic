package com.alfan.fanllymusic.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumArtUri: String?,
    val filePath: String,
    val durationMs: Long,
    val sampleRate: Int,
    val bitDepth: Int,
    val fileSizeBytes: Long,
    val isFavorite: Boolean = false,
    val dateAdded: Long
)
