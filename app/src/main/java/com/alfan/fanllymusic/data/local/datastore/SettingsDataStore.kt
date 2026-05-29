package com.alfan.fanllymusic.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.settingsDataStore by preferencesDataStore(name = "fanlly_settings")

object SettingsKeys {
    val CrossfadeSeconds = floatPreferencesKey("crossfade_seconds")
    val ReplayGain = booleanPreferencesKey("replay_gain")
    val GaplessPlayback = booleanPreferencesKey("gapless_playback")
    val AccentColor = stringPreferencesKey("accent_color")
    val AlbumArtRadius = floatPreferencesKey("album_art_radius")
    val MiniPlayerStyle = stringPreferencesKey("mini_player_style")
    val BlurIntensity = floatPreferencesKey("blur_intensity")
    val SortBy = stringPreferencesKey("sort_by")
    val SortAscending = booleanPreferencesKey("sort_ascending")
    val GroupBy = stringPreferencesKey("group_by")
}
