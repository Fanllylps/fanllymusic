package com.alfan.fanllymusic.util

import com.alfan.fanllymusic.domain.model.LyricLine
import java.io.File
import javax.inject.Inject

class LrcParser @Inject constructor() {
    private val timestampRegex = Regex("\\[(\\d{1,2}):(\\d{2})(?:[.:](\\d{1,3}))?]\\s*")

    fun parse(rawLrc: String): List<LyricLine> {
        val synced = rawLrc.lines()
            .flatMap { line ->
                val matches = timestampRegex.findAll(line).toList()
                if (matches.isEmpty()) {
                    emptyList()
                } else {
                    val text = timestampRegex.replace(line, "").trim()
                    matches.map { match ->
                        val (min, sec, fraction) = match.destructured
                        val timeMs = (min.toLong() * 60 * 1000) +
                            (sec.toLong() * 1000) +
                            fraction.padEnd(3, '0').take(3).toLongOrNull().orZero()
                        LyricLine(timestampMs = timeMs, text = text)
                    }
                }
            }
            .filter { it.text.isNotBlank() }
            .sortedBy { it.timestampMs }

        if (synced.isNotEmpty()) return synced

        // Plain/unsynced text fallback: strip LRC metadata tags, keep content lines.
        // timestampMs = -1L signals unsynced to LyricsView.
        return rawLrc.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("[") }
            .map { LyricLine(timestampMs = -1L, text = it) }
    }
}

fun findLrcFile(flacPath: String): File? =
    LocalLyricsDiscovery(EmbeddedLyricsReader()).findExactSidecar(File(flacPath), "lrc")

private fun Long?.orZero(): Long = this ?: 0L
