package com.alfan.fanllymusic.util

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.alfan.fanllymusic.data.local.entity.SongEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MediaStoreHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioQualityDetector: AudioQualityDetector
) {
    suspend fun scanFlacSongs(): List<SongEntity> = withContext(Dispatchers.IO) {
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.SIZE
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC}=1 AND (${MediaStore.Audio.Media.MIME_TYPE}=? OR ${MediaStore.Audio.Media.MIME_TYPE}=?)"
        val args = arrayOf("audio/flac", "audio/x-flac")
        val songs = mutableListOf<SongEntity>()
        context.contentResolver.query(collection, projection, selection, args, "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC")?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val filePath = cursor.getString(pathColumn).orEmpty()
                val quality = audioQualityDetector.detect(filePath)
                val albumId = cursor.getLong(albumIdColumn)
                val albumArtUri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumId).toString()
                songs += SongEntity(
                    id = id,
                    title = cursor.getString(titleColumn).orEmpty(),
                    artist = cursor.getString(artistColumn).orEmpty(),
                    album = cursor.getString(albumColumn).orEmpty(),
                    albumArtUri = albumArtUri,
                    filePath = filePath,
                    durationMs = cursor.getLong(durationColumn),
                    sampleRate = quality.first,
                    bitDepth = quality.second,
                    fileSizeBytes = cursor.getLong(sizeColumn),
                    dateAdded = cursor.getLong(dateAddedColumn)
                )
            }
        }
        songs
    }
}
