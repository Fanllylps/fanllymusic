package com.alfan.fanllymusic.util

import com.alfan.fanllymusic.domain.model.LyricsState
import org.junit.Assert.assertTrue
import org.junit.Test

class LyricsTextClassifierTest {
    private val parser = LrcParser()

    @Test
    fun toStateReturnsSyncedLyricsForTimestampedLrc() {
        val state = LyricsTextClassifier.toState("[00:01.00]Hello", parser)

        assertTrue(state is LyricsState.SyncedLyricsFound)
    }

    @Test
    fun toStateReturnsUnsyncedLyricsForPlainText() {
        val state = LyricsTextClassifier.toState("Line one\nLine two\nLine three", parser)

        assertTrue(state is LyricsState.UnsyncedLyricsFound)
    }
}
