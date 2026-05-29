package com.alfan.fanllymusic.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "artwork_colors")
data class ArtworkColorEntity(
    @PrimaryKey val songKey: String,
    val colorInt: Int,
    val extractedAt: Long
)
