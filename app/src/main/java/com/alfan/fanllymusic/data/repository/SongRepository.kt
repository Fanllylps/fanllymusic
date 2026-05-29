package com.alfan.fanllymusic.data.repository

import com.alfan.fanllymusic.data.local.dao.MetadataOverrideDao
import com.alfan.fanllymusic.data.local.dao.SongDao
import com.alfan.fanllymusic.data.local.entity.MetadataOverrideEntity
import com.alfan.fanllymusic.data.local.entity.SongEntity
import com.alfan.fanllymusic.domain.model.MetadataOverride
import com.alfan.fanllymusic.domain.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongRepository @Inject constructor(
    private val songDao: SongDao,
    private val metadataOverrideDao: MetadataOverrideDao
) {
    fun observeSongs(): Flow<List<Song>> =
        combine(songDao.observeSongs(), metadataOverrideDao.observeOverrides()) { songs, overrides ->
            val overrideByKey = overrides.associateBy { it.songKey }
            songs.map { entity -> entity.toDomain(overrideByKey[entity.songKey]) }
        }

    fun observeSongEntities(): Flow<List<SongEntity>> = songDao.observeSongs()

    suspend fun refreshSongs(songs: List<SongEntity>) {
        songDao.replaceAll(songs)
    }

    suspend fun setFavorite(songId: Long, favorite: Boolean) {
        songDao.setFavorite(songId, favorite)
    }

    suspend fun saveMetadataOverride(song: Song, override: MetadataOverride) {
        metadataOverrideDao.upsert(
            MetadataOverrideEntity(
                songKey = song.songKey,
                title = override.title.cleanOverride(),
                artist = override.artist.cleanOverride(),
                album = override.album.cleanOverride(),
                genre = override.genre.cleanOverride(),
                year = override.year,
                trackNumber = override.trackNumber,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun resetMetadataOverride(song: Song) {
        metadataOverrideDao.delete(song.songKey)
    }

    private val SongEntity.songKey: String
        get() = filePath.ifBlank { id.toString() }

    private fun SongEntity.toDomain(override: MetadataOverrideEntity?): Song {
        return Song(
            id = id,
            title = override?.title ?: title,
            artist = override?.artist ?: artist,
            album = override?.album ?: album,
            albumArtUri = albumArtUri,
            filePath = filePath,
            durationMs = durationMs,
            sampleRate = sampleRate,
            bitDepth = bitDepth,
            fileSizeBytes = fileSizeBytes,
            isFavorite = isFavorite,
            dateAdded = dateAdded,
            genre = override?.genre,
            year = override?.year,
            trackNumber = override?.trackNumber,
            metadataOverridden = override != null
        )
    }

    private fun String?.cleanOverride(): String? = this?.trim()?.takeIf { it.isNotBlank() }
}
