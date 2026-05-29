package com.alfan.fanllymusic.domain.model

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumArtUri: String?,
    val filePath: String,
    val durationMs: Long,
    val sampleRate: Int,
    val bitDepth: Int,
    val fileSizeBytes: Long,
    val isFavorite: Boolean,
    val dateAdded: Long,
    val genre: String? = null,
    val year: Int? = null,
    val trackNumber: Int? = null,
    val metadataOverridden: Boolean = false
) {
    val songKey: String
        get() = filePath.ifBlank { id.toString() }

    private val filenameBase: String
        get() = filePath
            .substringAfterLast('/')
            .substringBeforeLast('.', missingDelimiterValue = filePath.substringAfterLast('/'))

    private val filenameArtistTitle: Pair<String, String>?
        get() {
            val parts = filenameBase.split(" - ", limit = 2)
            if (parts.size != 2) return null
            val parsedArtist = parts[0].trim()
            val parsedTitle = parts[1].trim()
            return if (parsedArtist.isNotBlank() && parsedTitle.isNotBlank()) {
                parsedArtist to parsedTitle
            } else {
                null
            }
        }

    val displayTitle: String
        get() = title.ifBlank { filenameArtistTitle?.second ?: filenameBase }

    val displayArtist: String
        get() {
            val parsed = filenameArtistTitle
            if (parsed != null && title.isNotBlank() && normalize(parsed.second) == normalize(title)) {
                val parsedArtist = parsed.first
                if (parsedArtist.isNotBlank() && normalize(parsedArtist) != normalize(artist)) {
                    return parsedArtist
                }
            }
            return artist.ifBlank { parsed?.first ?: "Unknown Artist" }
        }

    val quality: AudioQuality
        get() = when {
            sampleRate >= 88_200 && bitDepth >= 24 -> AudioQuality.HI_RES_LOSSLESS
            sampleRate == 44_100 && bitDepth == 16 -> AudioQuality.LOSSLESS
            else -> AudioQuality.STANDARD
        }

    private fun normalize(value: String): String =
        value.lowercase().replace(Regex("[^a-z0-9]+"), "")
}
