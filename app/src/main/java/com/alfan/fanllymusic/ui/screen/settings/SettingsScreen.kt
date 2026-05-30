package com.alfan.fanllymusic.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alfan.fanllymusic.data.local.datastore.SettingsKeys
import com.alfan.fanllymusic.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val prefs by viewModel.preferences.collectAsState()

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
                SettingRowAction("System Equalizer", "Open device audio effects") {
                    // Handled in ViewModel / Intent later
                }
                SliderSetting("Bass Boost", "${(prefs?.get(SettingsKeys.BassBoost) ?: 0f).toInt()}%", prefs?.get(SettingsKeys.BassBoost) ?: 0f, 0f..100f) {
                    viewModel.saveFloat(SettingsKeys.BassBoost, it)
                }
                ChoiceSetting("Virtualizer", listOf("Off", "Low", "Medium", "High"), prefs?.get(SettingsKeys.Virtualizer) ?: "Off") {
                    viewModel.saveString(SettingsKeys.Virtualizer, it)
                }
                ChoiceSetting("Volume Normalization", listOf("Off", "Track", "Album"), prefs?.get(SettingsKeys.VolumeNormalization) ?: "Off") {
                    viewModel.saveString(SettingsKeys.VolumeNormalization, it)
                }
                ToggleSetting("Gapless Playback", prefs?.get(SettingsKeys.GaplessPlayback) ?: true) {
                    viewModel.saveBoolean(SettingsKeys.GaplessPlayback, it)
                }
                SliderSetting("Crossfade", "${(prefs?.get(SettingsKeys.CrossfadeSeconds) ?: 0f).toInt()}s", prefs?.get(SettingsKeys.CrossfadeSeconds) ?: 0f, 0f..10f) {
                    viewModel.saveFloat(SettingsKeys.CrossfadeSeconds, it)
                }
            }
        }

        item {
            SettingsCard(icon = Icons.Rounded.PlayArrow, title = "Playback") {
                ToggleSetting("Resume last playback", prefs?.get(SettingsKeys.ResumeLastPlayback) ?: true) {
                    viewModel.saveBoolean(SettingsKeys.ResumeLastPlayback, it)
                }
                ChoiceSetting("Sleep Timer", listOf("Off", "15", "30", "60", "End of song", "End of queue"), prefs?.get(SettingsKeys.SleepTimerMinutes) ?: "Off") {
                    viewModel.saveString(SettingsKeys.SleepTimerMinutes, it)
                }
                ToggleSetting("Pause when headphone disconnected", prefs?.get(SettingsKeys.PauseHeadphonesDisconnected) ?: true) {
                    viewModel.saveBoolean(SettingsKeys.PauseHeadphonesDisconnected, it)
                }
                ToggleSetting("Resume when headphone connected", prefs?.get(SettingsKeys.ResumeHeadphonesConnected) ?: false) {
                    viewModel.saveBoolean(SettingsKeys.ResumeHeadphonesConnected, it)
                }
                ChoiceSetting("Playback Speed", listOf("0.75", "1.0", "1.25", "1.5", "2.0"), (prefs?.get(SettingsKeys.PlaybackSpeed) ?: 1.0f).toString()) {
                    viewModel.saveFloat(SettingsKeys.PlaybackSpeed, it.toFloatOrNull() ?: 1.0f)
                }
            }
        }

        item {
            SettingsCard(icon = Icons.Rounded.FormatColorText, title = "Lyrics") {
                ToggleSetting("Auto-scroll lyrics", prefs?.get(SettingsKeys.LyricsAutoScroll) ?: true) {
                    viewModel.saveBoolean(SettingsKeys.LyricsAutoScroll, it)
                }
                ChoiceSetting("Font size", listOf("Small", "Medium", "Large", "Extra Large"), prefs?.get(SettingsKeys.LyricsFontSize) ?: "Medium") {
                    viewModel.saveString(SettingsKeys.LyricsFontSize, it)
                }
                ChoiceSetting("Background", listOf("Blurred artwork", "Dark gradient", "Solid black"), prefs?.get(SettingsKeys.LyricsBackgroundStyle) ?: "Blurred artwork") {
                    viewModel.saveString(SettingsKeys.LyricsBackgroundStyle, it)
                }
                ToggleSetting("Search lyrics automatically", prefs?.get(SettingsKeys.SearchLyricsAutomatically) ?: true) {
                    viewModel.saveBoolean(SettingsKeys.SearchLyricsAutomatically, it)
                }
                ToggleSetting("Cache lyrics for offline", prefs?.get(SettingsKeys.CacheLyricsOffline) ?: true) {
                    viewModel.saveBoolean(SettingsKeys.CacheLyricsOffline, it)
                }
            }
        }

        item {
            SettingsCard(icon = Icons.Rounded.Palette, title = "Appearance") {
                ToggleSetting("Dynamic album art theme", prefs?.get(SettingsKeys.DynamicAlbumArtTheme) ?: true) {
                    viewModel.saveBoolean(SettingsKeys.DynamicAlbumArtTheme, it)
                }
                ChoiceSetting("Accent color", listOf("Pink", "Blue", "Purple", "Green", "System"), prefs?.get(SettingsKeys.AccentColor) ?: "Pink") {
                    viewModel.saveString(SettingsKeys.AccentColor, it)
                }
                ChoiceSetting("Mini player style", listOf("Compact", "Glass", "Minimal"), prefs?.get(SettingsKeys.MiniPlayerStyle) ?: "Glass") {
                    viewModel.saveString(SettingsKeys.MiniPlayerStyle, it)
                }
                SliderSetting("Album art corners", "${(prefs?.get(SettingsKeys.AlbumArtRadius) ?: 32f).toInt()}dp", prefs?.get(SettingsKeys.AlbumArtRadius) ?: 32f, 0f..48f) {
                    viewModel.saveFloat(SettingsKeys.AlbumArtRadius, it)
                }
                ToggleSetting("Show lossless badge", prefs?.get(SettingsKeys.ShowLosslessBadge) ?: true) {
                    viewModel.saveBoolean(SettingsKeys.ShowLosslessBadge, it)
                }
            }
        }

        item {
            SettingsCard(icon = Icons.Rounded.LibraryMusic, title = "Library") {
                ChoiceSetting("Sort songs by", listOf("Title", "Artist", "Date Added", "Duration"), prefs?.get(SettingsKeys.SortBy) ?: "Title") {
                    viewModel.saveString(SettingsKeys.SortBy, it)
                }
                ToggleSetting("Ascending sort", prefs?.get(SettingsKeys.SortAscending) ?: true) {
                    viewModel.saveBoolean(SettingsKeys.SortAscending, it)
                }
                ToggleSetting("Show only lossless", prefs?.get(SettingsKeys.ShowOnlyLossless) ?: false) {
                    viewModel.saveBoolean(SettingsKeys.ShowOnlyLossless, it)
                }
            }
        }

        item {
            SettingsCard(icon = Icons.Rounded.Storage, title = "Storage") {
                SettingRowAction("Clear artwork cache", "Free up space") {}
                SettingRowAction("Clear lyrics cache", "Delete saved lyrics") {}
                SettingRowAction("Clear playback history", "Reset history") {}
            }
        }

        item {
            SettingsCard(icon = Icons.Rounded.Info, title = "About") {
                SettingRow("App version", "1.0.0")
                SettingRow("Audio engine", "Media3 ExoPlayer")
                SettingRow("Credits", "FanllyMusic")
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChoiceSetting(label: String, options: List<String>, selected: String, onSelected: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, color = White, style = MaterialTheme.typography.bodyMedium)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEach { option ->
                FilterChip(selected = selected == option, onClick = { onSelected(option) }, label = { Text(option) })
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

@Composable
private fun SettingRowAction(title: String, subtitle: String, onClick: () -> Unit) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick() }
        .padding(vertical = 4.dp)) {
        Text(text = title, color = White, style = MaterialTheme.typography.bodyMedium)
        Text(text = subtitle, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
    }
}
