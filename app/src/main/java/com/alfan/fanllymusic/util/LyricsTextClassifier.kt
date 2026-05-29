package com.alfan.fanllymusic.util

import com.alfan.fanllymusic.domain.model.LyricLine
import com.alfan.fanllymusic.domain.model.LyricsState

object LyricsTextClassifier {
    private val lrcTimestamp = Regex("\\[\\d{1,2}:\\d{2}(?:[.:]\\d{1,3})?]")
    private val verseWords = Regex(
        "\\b(chorus|verse|bridge|intro|outro|lyrics|love|heart|baby|tonight|feel|dream|sing|song)\\b",
        RegexOption.IGNORE_CASE
    )

    fun hasSyncedTimestamp(text: String): Boolean = lrcTimestamp.containsMatchIn(text)

    fun looksLikeLyrics(text: String): Boolean {
        val cleanLines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (cleanLines.size >= 3) return true
        if (hasSyncedTimestamp(text)) return true
        if (cleanLines.any { verseWords.containsMatchIn(it) }) return true
        return cleanLines.any { line ->
            line.length in 12..120 && line.count { it.isWhitespace() } >= 3
        }
    }

    fun toState(rawLyrics: String, parser: LrcParser): LyricsState {
        val lines = parser.parse(rawLyrics)
        return lines.toLyricsState()
    }

    fun List<LyricLine>.toLyricsState(): LyricsState =
        when {
            isEmpty() -> LyricsState.NotFound
            any { it.timestampMs >= 0L } -> LyricsState.SyncedLyricsFound(this)
            else -> LyricsState.UnsyncedLyricsFound(this)
        }
}
