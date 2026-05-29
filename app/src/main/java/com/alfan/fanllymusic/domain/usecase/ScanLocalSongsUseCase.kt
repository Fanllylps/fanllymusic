package com.alfan.fanllymusic.domain.usecase

import com.alfan.fanllymusic.data.repository.SongRepository
import com.alfan.fanllymusic.data.local.entity.SongEntity
import com.alfan.fanllymusic.util.MediaStoreHelper
import javax.inject.Inject

class ScanLocalSongsUseCase @Inject constructor(
    private val helper: MediaStoreHelper,
    private val repo: SongRepository
) {
    suspend operator fun invoke(): List<SongEntity> {
        val songs = helper.scanFlacSongs()
        repo.refreshSongs(songs)
        return songs
    }
}
