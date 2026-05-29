package com.alfan.fanllymusic.domain.usecase

import android.util.Log
import com.alfan.fanllymusic.data.repository.LyricsRepository
import com.alfan.fanllymusic.domain.model.LyricsState
import com.alfan.fanllymusic.util.LrcParser
import com.alfan.fanllymusic.util.LocalLyricsDiscovery
import com.alfan.fanllymusic.util.LyricsLookupRequest
import com.alfan.fanllymusic.util.LyricsSource
import com.alfan.fanllymusic.util.LyricsTextClassifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FetchAndCacheLyricsUseCase @Inject constructor(
    private val repo: LyricsRepository,
    private val parser: LrcParser,
    private val localLyricsDiscovery: LocalLyricsDiscovery
) {
    suspend operator fun invoke(
        songId: Long,
        songKey: String = songId.toString(),
        artist: String,
        title: String,
        album: String,
        durationMs: Long,
        filePath: String = ""
    ): LyricsState = withContext(Dispatchers.IO) {
        Log.d(TAG, "Lookup songId=$songId title='$title' artist='$artist' filePath='$filePath'")

        val cachedByKey = repo.getCachedLyrics(songKey)
        if (cachedByKey != null) {
            val state = LyricsTextClassifier.toState(cachedByKey.rawLyrics, parser)
            Log.d(TAG, "cacheHit=true source=${cachedByKey.source} finalState=${state.debugName()}")
            return@withContext state
        }
        Log.d(TAG, "cacheHit=false")

        val request = LyricsLookupRequest(
            title = title,
            artist = artist,
            album = album,
            durationMs = durationMs,
            filePath = filePath
        )
        val checkedPaths = if (filePath.isNotBlank()) localLyricsDiscovery.checkedLrcPaths(request) else emptyList()

        val local = if (filePath.isNotBlank()) {
            localLyricsDiscovery.discover(request)
        } else {
            null
        }

        if (local != null) {
            val state = LyricsTextClassifier.toState(local.rawLyrics, parser)
            repo.saveLyrics(songId = songId, songKey = songKey, rawLyrics = local.rawLyrics, source = local.source.debugName())
            Log.d(TAG, "Checked .lrc paths=${local.checkedLrcPaths}")
            Log.d(
                TAG,
                "embeddedLyricsFound=${local.embeddedLyricsFound} sidecarLyricsFound=${local.sidecarLyricsFound} " +
                    "source=${local.source.debugName()} finalState=${state.debugName()}"
            )
            return@withContext state
        }

        if (checkedPaths.isNotEmpty()) Log.d(TAG, "Checked .lrc paths=$checkedPaths")

        val cached = repo.getCachedLyrics(songId)
        if (cached != null) {
            val state = LyricsTextClassifier.toState(cached.rawLrc, parser)
            Log.d(TAG, "embeddedLyricsFound=false sidecarLyricsFound=false source=database finalState=${state.debugName()}")
            return@withContext state
        }

        Log.d(TAG, "embeddedLyricsFound=false sidecarLyricsFound=false finalState=NotFound")
        return@withContext LyricsState.NotFound
    }

    suspend fun searchOnlineLyrics(
        songId: Long,
        songKey: String = songId.toString(),
        artist: String,
        title: String,
        album: String,
        durationMs: Long,
        filePath: String
    ): LyricsState = withContext(Dispatchers.IO) {
        val attempts = onlineSearchAttempts(artist, title, filePath)
        Log.d(TAG, "Online search songId=$songId attempts=$attempts durationMs=$durationMs")

        attempts.forEach { (attemptArtist, attemptTitle) ->
            val found = runCatching {
                repo.searchAndCacheLyrics(
                    songId = songId,
                    songKey = songKey,
                    artist = attemptArtist,
                    title = attemptTitle,
                    album = album,
                    durationSeconds = durationMs / 1000
                )
            }.getOrNull()

            if (found != null) {
                val state = LyricsTextClassifier.toState(found.rawLyrics, parser)
                Log.d(TAG, "Online lyrics found artist='$attemptArtist' title='$attemptTitle' finalState=${state.debugName()}")
                return@withContext state
            }
        }

        Log.d(TAG, "Online lyrics not found finalState=NotFound")
        LyricsState.NotFound
    }

    suspend fun saveManualLyrics(songId: Long, songKey: String, rawLrc: String): LyricsState = withContext(Dispatchers.IO) {
        repo.saveLyrics(songId = songId, songKey = songKey, rawLyrics = rawLrc, source = "manual")
        val state = LyricsTextClassifier.toState(rawLrc, parser)
        Log.d(TAG, "Manual lyrics saved songId=$songId songKey='$songKey' finalState=${state.debugName()}")
        return@withContext state
    }

    private fun onlineSearchAttempts(artist: String, title: String, filePath: String): List<Pair<String, String>> {
        val filenameBase = filePath.substringAfterLast('/').substringBeforeLast('.', filePath.substringAfterLast('/'))
        val filenameParts = filenameBase.split(" - ", limit = 2)
        return buildList {
            if (title.isNotBlank()) add(artist to title)
            if (filenameParts.size == 2 && filenameParts[0].isNotBlank() && filenameParts[1].isNotBlank()) {
                add(filenameParts[0].trim() to filenameParts[1].trim())
            }
            if (filenameBase.isNotBlank()) add("" to filenameBase.trim())
        }.distinctBy { (candidateArtist, candidateTitle) ->
            "${candidateArtist.lowercase()}|${candidateTitle.lowercase()}"
        }
    }

    private fun LyricsSource.debugName(): String = when (this) {
        is LyricsSource.Embedded -> "embedded:$fieldName"
        is LyricsSource.Sidecar -> "sidecar:${file.absolutePath}"
    }

    private fun LyricsState.debugName(): String = when (this) {
        LyricsState.Idle -> "Idle"
        LyricsState.LoadingLocal -> "LoadingLocal"
        LyricsState.LoadingOnline -> "LoadingOnline"
        is LyricsState.SyncedLyricsFound -> "SyncedLyricsFound(lines=${lines.size})"
        is LyricsState.UnsyncedLyricsFound -> "UnsyncedLyricsFound(lines=${lines.size})"
        LyricsState.NotFound -> "NotFound"
        is LyricsState.Error -> "Error(message=$message)"
    }

    companion object {
        private const val TAG = "FanllyLyrics"
    }
}
