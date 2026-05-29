package com.alfan.fanllymusic.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.alfan.fanllymusic.data.local.FanllyDatabase
import com.alfan.fanllymusic.data.remote.LrclibApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FanllyDatabase {
        return Room.databaseBuilder(context, FanllyDatabase::class.java, "fanlly_music.db")
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    @Provides
    fun provideSongDao(database: FanllyDatabase) = database.songDao()

    @Provides
    fun providePlaylistDao(database: FanllyDatabase) = database.playlistDao()

    @Provides
    fun provideLyricsDao(database: FanllyDatabase) = database.lyricsDao()

    @Provides
    fun provideHistoryDao(database: FanllyDatabase) = database.historyDao()

    @Provides
    fun provideMetadataOverrideDao(database: FanllyDatabase) = database.metadataOverrideDao()

    @Provides
    fun provideArtworkColorDao(database: FanllyDatabase) = database.artworkColorDao()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder().addInterceptor(logging).build()
    }

    @Provides
    @Singleton
    fun provideLrclibApi(client: OkHttpClient): LrclibApi {
        return Retrofit.Builder()
            .baseUrl("https://lrclib.net/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LrclibApi::class.java)
    }

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS metadata_overrides (
                    songKey TEXT NOT NULL PRIMARY KEY,
                    title TEXT,
                    artist TEXT,
                    album TEXT,
                    genre TEXT,
                    year INTEGER,
                    trackNumber INTEGER,
                    updatedAt INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS lyrics_cache (
                    songKey TEXT NOT NULL PRIMARY KEY,
                    rawLyrics TEXT NOT NULL,
                    source TEXT NOT NULL,
                    fetchedAt INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS artwork_colors (
                    songKey TEXT NOT NULL PRIMARY KEY,
                    colorInt INTEGER NOT NULL,
                    extractedAt INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }
    }
}
