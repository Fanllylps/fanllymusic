package com.alfan.fanllymusic.ui.screen.settings

import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alfan.fanllymusic.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val preferences: StateFlow<Preferences?> = settingsRepository.preferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun saveBoolean(key: Preferences.Key<Boolean>, value: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveBoolean(key, value)
        }
    }

    fun saveFloat(key: Preferences.Key<Float>, value: Float) {
        viewModelScope.launch {
            settingsRepository.saveFloat(key, value)
        }
    }

    fun saveString(key: Preferences.Key<String>, value: String) {
        viewModelScope.launch {
            settingsRepository.saveString(key, value)
        }
    }
}
