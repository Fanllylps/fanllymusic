package com.alfan.fanllymusic.data.repository

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.alfan.fanllymusic.data.local.datastore.SettingsKeys
import com.alfan.fanllymusic.data.local.datastore.settingsDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.settingsDataStore

    val preferencesFlow: Flow<Preferences> = dataStore.data

    suspend fun saveBoolean(key: Preferences.Key<Boolean>, value: Boolean) {
        dataStore.edit { it[key] = value }
    }

    suspend fun saveFloat(key: Preferences.Key<Float>, value: Float) {
        dataStore.edit { it[key] = value }
    }

    suspend fun saveString(key: Preferences.Key<String>, value: String) {
        dataStore.edit { it[key] = value }
    }
}
