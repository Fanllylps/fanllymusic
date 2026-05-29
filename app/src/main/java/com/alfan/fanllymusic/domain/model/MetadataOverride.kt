package com.alfan.fanllymusic.domain.model

data class MetadataOverride(
    val title: String?,
    val artist: String?,
    val album: String?,
    val genre: String?,
    val year: Int?,
    val trackNumber: Int?
)
