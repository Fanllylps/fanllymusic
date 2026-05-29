package com.alfan.fanllymusic.data.repository

import com.alfan.fanllymusic.data.local.dao.PlaylistDao
import com.alfan.fanllymusic.data.local.entity.PlaylistEntity
import com.alfan.fanllymusic.data.local.entity.PlaylistSongCrossRef
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepository @Inject constructor(
    private val playlistDao: PlaylistDao
) {
    fun observePlaylists(): Flow<List<PlaylistEntity>> = playlistDao.observePlaylists()

    suspend fun createPlaylist(name: String): Long {
        return playlistDao.upsert(PlaylistEntity(name = name, createdAt = System.currentTimeMillis()))
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        playlistDao.addSong(
            PlaylistSongCrossRef(
                playlistId = playlistId,
                songId = songId,
                addedAt = System.currentTimeMillis()
            )
        )
    }
}
