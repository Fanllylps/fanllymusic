package com.alfan.fanllymusic.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class SongTest {
    @Test
    fun displayArtistFallsBackToFilenameArtistWhenMetadataArtistLooksWrong() {
        val song = song(
            title = "BIRDS OF A FEATHER",
            artist = "Andrew Marshall",
            filePath = "/music/Billie Eilish - BIRDS OF A FEATHER.flac"
        )

        assertEquals("BIRDS OF A FEATHER", song.displayTitle)
        assertEquals("Billie Eilish", song.displayArtist)
    }

    @Test
    fun displayArtistKeepsMetadataWhenFilenameDoesNotMatchTitle() {
        val song = song(
            title = "Different Song",
            artist = "Andrew Marshall",
            filePath = "/music/Billie Eilish - BIRDS OF A FEATHER.flac"
        )

        assertEquals("Andrew Marshall", song.displayArtist)
    }

    private fun song(
        title: String,
        artist: String,
        filePath: String
    ) = Song(
        id = 1L,
        title = title,
        artist = artist,
        album = "",
        albumArtUri = null,
        filePath = filePath,
        durationMs = 0L,
        sampleRate = 44_100,
        bitDepth = 16,
        fileSizeBytes = 0L,
        isFavorite = false,
        dateAdded = 0L
    )
}
