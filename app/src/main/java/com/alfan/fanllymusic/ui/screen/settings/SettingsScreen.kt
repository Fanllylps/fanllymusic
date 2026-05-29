package com.alfan.fanllymusic.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import com.alfan.fanllymusic.data.local.datastore.SettingsKeys
import com.alfan.fanllymusic.data.local.datastore.settingsDataStore
import com.alfan.fanllymusic.ui.theme.AccentPink
import com.alfan.fanllymusic.ui.theme.Black
import com.alfan.fanllymusic.ui.theme.DarkSurfaceVariant
import com.alfan.fanllymusic.ui.theme.TextSecondary
import com.alfan.fanllymusic.ui.theme.White
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val prefs by context.settingsDataStore.data.collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    fun saveBoolean(key: androidx.datastore.preferences.core.Preferences.Key<Boolean>, value: Boolean) {
        scope.launch { context.settingsDataStore.edit { it[key] = value } }
    }

    fun saveFloat(key: androidx.datastore.preferences.core.Preferences.Key<Float>, value: Float) {
        scope.launch { context.settingsDataStore.edit { it[key] = value } }
    }

    fun saveString(key: androidx.datastore.preferences.core.Preferences.Key<String>, value: String) {
        scope.launch { context.settingsDataStore.edit { it[key] = value } }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Settings", style = MaterialTheme.typography.displayLarge, color = White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Tune FanllyMusic for local lossless playback.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            SettingsCard(icon = Icons.Rounded.GraphicEq, title = "Audio") {
                SliderSetting("Crossfade", "${(prefs?.get(SettingsKeys.CrossfadeSeconds) ?: 0f).toInt()}s", prefs?.get(SettingsKeys.CrossfadeSeconds) ?: 0f, 0f..10f) {
                    saveFloat(SettingsKeys.CrossfadeSeconds, it)
                }
                ToggleSetting("ReplayGain", prefs?.get(SettingsKeys.ReplayGain) ?: false) { saveBoolean(SettingsKeys.ReplayGain, it) }
                ToggleSetting("Gapless playback", prefs?.get(SettingsKeys.GaplessPlayback) ?: true) { saveBoolean(SettingsKeys.GaplessPlayback, it) }
            }
        }

        item {
            SettingsCard(icon = Icons.Rounded.Palette, title = "Appearance") {
                ChoiceSetting("Accent color", listOf("Red", "Pink", "Purple", "Blue", "Teal"), prefs?.get(SettingsKeys.AccentColor) ?: "Pink") {
                    saveString(SettingsKeys.AccentColor, it)
                }
                SliderSetting("Album art corners", "${(prefs?.get(SettingsKeys.AlbumArtRadius) ?: 32f).toInt()}dp", prefs?.get(SettingsKeys.AlbumArtRadius) ?: 32f, 0f..32f) {
                    saveFloat(SettingsKeys.AlbumArtRadius, it)
                }
                ChoiceSetting("Mini player", listOf("Compact", "Expanded"), prefs?.get(SettingsKeys.MiniPlayerStyle) ?: "Expanded") {
                    saveString(SettingsKeys.MiniPlayerStyle, it)
                }
                SliderSetting("Blur intensity", "${(prefs?.get(SettingsKeys.BlurIntensity) ?: 18f).toInt()}", prefs?.get(SettingsKeys.BlurIntensity) ?: 18f, 0f..25f) {
                    saveFloat(SettingsKeys.BlurIntensity, it)
                }
            }
        }

        item {
            SettingsCard(icon = Icons.Rounded.LibraryMusic, title = "Library") {
                ChoiceSetting("Sort songs by", listOf("Name", "Artist", "Date Added", "Duration"), prefs?.get(SettingsKeys.SortBy) ?: "Name") {
                    saveString(SettingsKeys.SortBy, it)
                }
                ToggleSetting("Ascending sort", prefs?.get(SettingsKeys.SortAscending) ?: true) { saveBoolean(SettingsKeys.SortAscending, it) }
                ChoiceSetting("Group by", listOf("All Songs", "Album", "Artist"), prefs?.get(SettingsKeys.GroupBy) ?: "All Songs") {
                    saveString(SettingsKeys.GroupBy, it)
                }
                Text("Scan folders and exclude folders coming next.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }

        item {
            SettingsCard(icon = Icons.Rounded.Info, title = "About") {
                SettingRow("FanllyMusic version", "1.0")
                SettingRow("Audio engine", "Media3 ExoPlayer")
            }
            Spacer(modifier = Modifier.height(180.dp))
        }
    }
}

@Composable
private fun SettingsCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = AccentPink)
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = title, color = White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            content()
        }
    }
}

@Composable
private fun ToggleSetting(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = White, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun SliderSetting(label: String, valueText: String, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = White, style = MaterialTheme.typography.bodyMedium)
            Text(valueText, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
        Slider(value = value, onValueChange = onChange, valueRange = range)
    }
}

@Composable
private fun ChoiceSetting(label: String, options: List<String>, selected: String, onSelected: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, color = White, style = MaterialTheme.typography.bodyMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            options.take(4).forEach { option ->
                FilterChip(selected = selected == option, onClick = { onSelected(option) }, label = { Text(option) })
            }
        }
        if (options.size > 4) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                options.drop(4).forEach { option ->
                    FilterChip(selected = selected == option, onClick = { onSelected(option) }, label = { Text(option) })
                }
            }
        }
    }
}

@Composable
private fun SettingRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, color = White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
