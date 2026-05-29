package com.alfan.fanllymusic.data.repository

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.palette.graphics.Palette
import com.alfan.fanllymusic.data.local.dao.ArtworkColorDao
import com.alfan.fanllymusic.data.local.entity.ArtworkColorEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArtworkColorRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val artworkColorDao: ArtworkColorDao
) {
    suspend fun getOrExtractColor(songKey: String, artworkUri: String?): Int? = withContext(Dispatchers.IO) {
        artworkColorDao.getColor(songKey)?.let { return@withContext it.colorInt }
        if (artworkUri.isNullOrBlank()) return@withContext null

        val bitmap = runCatching {
            context.contentResolver.openInputStream(Uri.parse(artworkUri))?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        }.getOrNull() ?: return@withContext null

        val palette = Palette.from(bitmap).maximumColorCount(12).generate()
        val color = palette.vibrantSwatch?.rgb
            ?: palette.dominantSwatch?.rgb
            ?: palette.mutedSwatch?.rgb
            ?: return@withContext null

        artworkColorDao.upsert(
            ArtworkColorEntity(
                songKey = songKey,
                colorInt = color,
                extractedAt = System.currentTimeMillis()
            )
        )
        color
    }
}
