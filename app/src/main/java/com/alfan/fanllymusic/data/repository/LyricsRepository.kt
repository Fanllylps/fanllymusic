package com.alfan.fanllymusic.data.repository

import com.alfan.fanllymusic.data.local.dao.LyricsDao
import com.alfan.fanllymusic.data.local.entity.LyricsCacheEntity
import com.alfan.fanllymusic.data.local.entity.LyricsEntity
import com.alfan.fanllymusic.data.remote.LrclibApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LyricsRepository @Inject constructor(
    private val lyricsDao: LyricsDao,
    private val lrclibApi: LrclibApi
) {
    suspend fun getCachedLyrics(songId: Long): LyricsEntity? = lyricsDao.getLyrics(songId)

    suspend fun getCachedLyrics(songKey: String): LyricsCacheEntity? = lyricsDao.getCachedLyrics(songKey)

    suspend fun saveLyrics(songId: Long, rawLrc: String) {
        lyricsDao.upsert(LyricsEntity(songId = songId, rawLrc = rawLrc, fetchedAt = System.currentTimeMillis()))
    }

    suspend fun saveLyrics(songId: Long, songKey: String, rawLyrics: String, source: String) {
        val now = System.currentTimeMillis()
        lyricsDao.upsert(LyricsEntity(songId = songId, rawLrc = rawLyrics, fetchedAt = now))
        lyricsDao.upsertCache(
            LyricsCacheEntity(
                songKey = songKey,
                rawLyrics = rawLyrics,
                source = source,
                fetchedAt = now
            )
        )
    }

    suspend fun fetchAndCacheLyrics(songId: Long, artist: String, title: String, album: String, durationSeconds: Long): LyricsEntity {
        val response = lrclibApi.getLyrics(artist, title, album, durationSeconds)
        val raw = response.syncedLyrics ?: response.plainLyrics.orEmpty()
        val entity = LyricsEntity(songId = songId, rawLrc = raw, fetchedAt = System.currentTimeMillis())
        lyricsDao.upsert(entity)
        return entity
    }

    suspend fun searchAndCacheLyrics(
        songId: Long,
        artist: String,
        title: String,
        album: String,
        durationSeconds: Long
    ): LyricsEntity? {
        val response = lrclibApi.getLyrics(artist, title, album, durationSeconds)
        val raw = response.syncedLyrics?.takeIf { it.isNotBlank() }
            ?: response.plainLyrics?.takeIf { it.isNotBlank() }
            ?: return null
        val entity = LyricsEntity(songId = songId, rawLrc = raw, fetchedAt = System.currentTimeMillis())
        lyricsDao.upsert(entity)
        return entity
    }

    suspend fun searchAndCacheLyrics(
        songId: Long,
        songKey: String,
        artist: String,
        title: String,
        album: String,
        durationSeconds: Long
    ): LyricsCacheEntity? {
        val response = lrclibApi.getLyrics(artist, title, album, durationSeconds)
        val raw = response.syncedLyrics?.takeIf { it.isNotBlank() }
            ?: response.plainLyrics?.takeIf { it.isNotBlank() }
            ?: return null
        val now = System.currentTimeMillis()
        lyricsDao.upsert(LyricsEntity(songId = songId, rawLrc = raw, fetchedAt = now))
        val entity = LyricsCacheEntity(
            songKey = songKey,
            rawLyrics = raw,
            source = if (response.syncedLyrics?.isNotBlank() == true) "lrclib_synced" else "lrclib_plain",
            fetchedAt = now
        )
        lyricsDao.upsertCache(entity)
        return entity
    }
}
