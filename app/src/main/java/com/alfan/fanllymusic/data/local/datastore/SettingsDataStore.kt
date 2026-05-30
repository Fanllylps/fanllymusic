package com.alfan.fanllymusic.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.settingsDataStore by preferencesDataStore(name = "fanlly_settings")

object SettingsKeys {
    // 1. Audio
    val BassBoost = floatPreferencesKey("bass_boost") // 0.0 - 100.0
    val Virtualizer = stringPreferencesKey("virtualizer") // Off, Low, Medium, High
    val VolumeNormalization = stringPreferencesKey("volume_normalization") // Off, Track, Album
    val GaplessPlayback = booleanPreferencesKey("gapless_playback")
    val CrossfadeSeconds = floatPreferencesKey("crossfade_seconds") // 0f, 3f, 5f, 8f, 10f

    // 2. Playback Comfort
    val ResumeLastPlayback = booleanPreferencesKey("resume_last_playback")
    val SleepTimerMinutes = stringPreferencesKey("sleep_timer_minutes") // Off, 15, 30, 60, End of song, End of queue
    val PauseHeadphonesDisconnected = booleanPreferencesKey("pause_headphones_disconnected")
    val ResumeHeadphonesConnected = booleanPreferencesKey("resume_headphones_connected")
    val PauseOnCall = booleanPreferencesKey("pause_on_call")
    val PlaybackSpeed = floatPreferencesKey("playback_speed") // 0.75f, 1.0f, 1.25f, 1.5f, 2.0f

    // 3. Lyrics
    val SearchLyricsAutomatically = booleanPreferencesKey("search_lyrics_auto")
    val CacheLyricsOffline = booleanPreferencesKey("cache_lyrics_offline")
    val LyricsFontSize = stringPreferencesKey("lyrics_font_size") // Small, Medium, Large, Extra Large
    val LyricsBackgroundStyle = stringPreferencesKey("lyrics_bg_style") // Blurred artwork, Dark gradient, Solid black
    val LyricsAutoScroll = booleanPreferencesKey("lyrics_auto_scroll")

    // 4. Appearance
    val DynamicAlbumArtTheme = booleanPreferencesKey("dynamic_album_art_theme")
    val AccentColor = stringPreferencesKey("accent_color") // Pink, Blue, Purple, Green, System
    val MiniPlayerStyle = stringPreferencesKey("mini_player_style") // Compact, Glass, Minimal
    val AlbumArtRadius = floatPreferencesKey("album_art_radius") // 0f (Square), 16f (Soft), 32f (Rounded), 48f (Extra Rounded)
    val ShowLosslessBadge = booleanPreferencesKey("show_lossless_badge")

    // 5. Library
    val SortBy = stringPreferencesKey("sort_by") // Title, Artist, Date Added, Duration
    val SortAscending = booleanPreferencesKey("sort_ascending")
    val ShowOnlyLossless = booleanPreferencesKey("show_only_lossless")
}
