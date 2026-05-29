package com.alfan.fanllymusic.data.repository

import com.alfan.fanllymusic.data.local.dao.HistoryDao
import com.alfan.fanllymusic.data.local.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepository @Inject constructor(
    private val historyDao: HistoryDao
) {
    fun observeHistory(): Flow<List<HistoryEntity>> = historyDao.observeHistory()

    suspend fun recordPlayed(songId: Long) {
        historyDao.insert(HistoryEntity(songId = songId, playedAt = System.currentTimeMillis()))
    }
}
