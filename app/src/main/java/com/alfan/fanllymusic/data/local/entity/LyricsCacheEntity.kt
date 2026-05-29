package com.alfan.fanllymusic.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lyrics_cache")
data class LyricsCacheEntity(
    @PrimaryKey val songKey: String,
    val rawLyrics: String,
    val source: String,
    val fetchedAt: Long
)
