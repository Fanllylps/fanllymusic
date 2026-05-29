package com.alfan.fanllymusic.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lyrics")
data class LyricsEntity(
    @PrimaryKey val songId: Long,
    val rawLrc: String,
    val fetchedAt: Long
)
