package com.alfan.fanllymusic.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

class LocalLyricsDiscoveryTest {
    private val discovery = LocalLyricsDiscovery(EmbeddedLyricsReader())

    @Test
    fun findsExactLrcNextToAudioForAnyExtension() {
        val dir = Files.createTempDirectory("lyrics-exact").toFile()
        val audio = File(dir, "song.mp3").apply { writeText("audio") }
        File(dir, "song.lrc").writeText("[00:01.00]Hello")

        val result = discovery.discover(
            LyricsLookupRequest(
                title = "Wrong Title",
                artist = "Wrong Artist",
                album = "",
                durationMs = 0L,
                filePath = audio.absolutePath
            )
        )

        assertEquals("[00:01.00]Hello", result?.rawLyrics)
        assertTrue(result?.source is LyricsSource.Sidecar)
    }

    @Test
    fun fuzzyMatchesTitleWhenArtistMetadataIsWrong() {
        val dir = Files.createTempDirectory("lyrics-fuzzy").toFile()
        val audio = File(dir, "Billie Eilish - BIRDS OF A FEATHER.flac").apply { writeText("audio") }
        File(dir, "birds of a feather.lrc").writeText("[00:02.00]I want you to stay")

        val result = discovery.discover(
            LyricsLookupRequest(
                title = "BIRDS OF A FEATHER",
                artist = "Andrew Marshall",
                album = "",
                durationMs = 0L,
                filePath = audio.absolutePath
            )
        )

        assertEquals("[00:02.00]I want you to stay", result?.rawLyrics)
        assertTrue(result?.source is LyricsSource.Sidecar)
    }
}
