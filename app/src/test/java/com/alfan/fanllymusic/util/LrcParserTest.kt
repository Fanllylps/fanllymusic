package com.alfan.fanllymusic.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LrcParserTest {
    private val parser = LrcParser()

    @Test
    fun parseSupportsMultipleTimestampsOnOneLine() {
        val lines = parser.parse("[00:10.50][00:20.500]Same lyric")

        assertEquals(2, lines.size)
        assertEquals(10_500L, lines[0].timestampMs)
        assertEquals("Same lyric", lines[0].text)
        assertEquals(20_500L, lines[1].timestampMs)
        assertEquals("Same lyric", lines[1].text)
    }

    @Test
    fun parseSupportsTimestampWithoutFraction() {
        val lines = parser.parse("[01:02]Line without millisecond fraction")

        assertEquals(1, lines.size)
        assertEquals(62_000L, lines.single().timestampMs)
        assertEquals("Line without millisecond fraction", lines.single().text)
    }

    @Test
    fun parsePlainTextAsUnsyncedLyrics() {
        val lines = parser.parse("[ar:Someone]\nFirst line\n\nSecond line")

        assertEquals(listOf("First line", "Second line"), lines.map { it.text })
        assertTrue(lines.all { it.timestampMs == -1L })
    }
}
