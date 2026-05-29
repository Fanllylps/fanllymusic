package com.alfan.fanllymusic.util

import java.io.File
import javax.inject.Inject

data class LyricsLookupRequest(
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val filePath: String
)

data class LyricsDiscoveryResult(
    val rawLyrics: String,
    val source: LyricsSource,
    val checkedLrcPaths: List<String>,
    val embeddedLyricsFound: Boolean,
    val sidecarLyricsFound: Boolean
)

sealed interface LyricsSource {
    data class Embedded(val fieldName: String) : LyricsSource
    data class Sidecar(val file: File) : LyricsSource
}

class LocalLyricsDiscovery @Inject constructor(
    private val embeddedLyricsReader: EmbeddedLyricsReader
) {
    fun discover(request: LyricsLookupRequest): LyricsDiscoveryResult? {
        val audioFile = File(request.filePath)
        val checkedLrcPaths = checkedLrcPaths(request).toMutableList()

        embeddedLyricsReader.read(audioFile)?.let { embedded ->
            return LyricsDiscoveryResult(
                rawLyrics = embedded.rawLyrics,
                source = LyricsSource.Embedded(embedded.fieldName),
                checkedLrcPaths = checkedLrcPaths,
                embeddedLyricsFound = true,
                sidecarLyricsFound = false
            )
        }

        val exactLrc = findExactSidecar(audioFile, "lrc")
        exactLrc?.readTextSafely()?.let { raw ->
            return LyricsDiscoveryResult(raw, LyricsSource.Sidecar(exactLrc), checkedLrcPaths, false, true)
        }

        val exactTxt = findExactSidecar(audioFile, "txt")
        exactTxt?.readTextSafely()?.let { raw ->
            return LyricsDiscoveryResult(raw, LyricsSource.Sidecar(exactTxt), checkedLrcPaths, false, true)
        }

        val fuzzyLrc = findFuzzySidecar(audioFile, fuzzyBaseNameCandidates(request, audioFile))
        fuzzyLrc?.readTextSafely()?.let { raw ->
            return LyricsDiscoveryResult(raw, LyricsSource.Sidecar(fuzzyLrc), checkedLrcPaths.distinct(), false, true)
        }

        return null
    }

    fun findExactSidecar(audioFile: File, extension: String): File? {
        if (audioFile.nameWithoutExtension.isBlank()) return null
        return audioFile.parentFile
            ?.resolve("${audioFile.nameWithoutExtension}.$extension")
            ?.takeIf { it.isFile }
    }

    fun checkedLrcPaths(request: LyricsLookupRequest): List<String> {
        val audioFile = File(request.filePath)
        val candidates = listOf(exactSidecarPath(audioFile, "lrc")) +
            fuzzyBaseNameCandidates(request, audioFile).mapNotNull { candidate ->
                audioFile.parentFile?.resolve("$candidate.lrc")?.absolutePath
            }
        return candidates.distinct()
    }

    private fun findFuzzySidecar(audioFile: File, candidates: List<String>): File? {
        val directory = audioFile.parentFile ?: return null
        val normalizedCandidates = candidates.map(::normalizeForMatch).filter { it.isNotBlank() }.toSet()
        if (normalizedCandidates.isEmpty()) return null

        return directory.listFiles()
            .orEmpty()
            .asSequence()
            .filter { it.isFile && it.extension.equals("lrc", ignoreCase = true) }
            .firstOrNull { normalizeForMatch(it.nameWithoutExtension) in normalizedCandidates }
    }

    private fun fuzzyBaseNameCandidates(request: LyricsLookupRequest, audioFile: File): List<String> {
        val title = request.title.ifBlank { audioFile.nameWithoutExtension }
        val artist = request.artist
        return buildList {
            add(title)
            if (artist.isNotBlank()) {
                add("$artist - $title")
                add("$title - $artist")
            }
            add(audioFile.nameWithoutExtension)
        }.map { it.trim() }.filter { it.isNotBlank() }.distinct()
    }

    private fun exactSidecarPath(audioFile: File, extension: String): String =
        audioFile.parentFile?.resolve("${audioFile.nameWithoutExtension}.$extension")?.absolutePath
            ?: "${audioFile.nameWithoutExtension}.$extension"

    companion object {
        fun normalizeForMatch(value: String): String {
            return value
                .lowercase()
                .replace(Regex("\\((feat\\.?|ft\\.?).*?\\)"), " ")
                .replace(Regex("\\[(feat\\.?|ft\\.?).*?\\]"), " ")
                .replace(Regex("\\b(feat\\.?|ft\\.?)\\b.*$"), " ")
                .replace(Regex("[^a-z0-9]+"), " ")
                .trim()
                .replace(Regex("\\s+"), " ")
        }
    }
}

private fun File.readTextSafely(): String? =
    runCatching { readText() }
        .getOrNull()
        ?.takeIf { it.isNotBlank() }
