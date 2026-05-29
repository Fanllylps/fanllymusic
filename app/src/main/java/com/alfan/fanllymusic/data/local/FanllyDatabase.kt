package com.alfan.fanllymusic.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.alfan.fanllymusic.data.local.dao.ArtworkColorDao
import com.alfan.fanllymusic.data.local.dao.HistoryDao
import com.alfan.fanllymusic.data.local.dao.LyricsDao
import com.alfan.fanllymusic.data.local.dao.MetadataOverrideDao
import com.alfan.fanllymusic.data.local.dao.PlaylistDao
import com.alfan.fanllymusic.data.local.dao.SongDao
import com.alfan.fanllymusic.data.local.entity.ArtworkColorEntity
import com.alfan.fanllymusic.data.local.entity.HistoryEntity
import com.alfan.fanllymusic.data.local.entity.LyricsCacheEntity
import com.alfan.fanllymusic.data.local.entity.LyricsEntity
import com.alfan.fanllymusic.data.local.entity.MetadataOverrideEntity
import com.alfan.fanllymusic.data.local.entity.PlaylistEntity
import com.alfan.fanllymusic.data.local.entity.PlaylistSongCrossRef
import com.alfan.fanllymusic.data.local.entity.SongEntity

@Database(
    entities = [
        SongEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        LyricsEntity::class,
        LyricsCacheEntity::class,
        HistoryEntity::class,
        MetadataOverrideEntity::class,
        ArtworkColorEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class FanllyDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun lyricsDao(): LyricsDao
    abstract fun historyDao(): HistoryDao
    abstract fun metadataOverrideDao(): MetadataOverrideDao
    abstract fun artworkColorDao(): ArtworkColorDao
}
