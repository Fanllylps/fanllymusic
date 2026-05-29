package com.alfan.fanllymusic.ui.screen.nowplaying

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import coil.compose.rememberAsyncImagePainter
import com.alfan.fanllymusic.domain.model.LyricsState
import com.alfan.fanllymusic.domain.model.Playlist
import com.alfan.fanllymusic.domain.model.Song
import com.alfan.fanllymusic.ui.components.BlurredBackground
import com.alfan.fanllymusic.ui.components.LyricsView
import com.alfan.fanllymusic.ui.components.QualityBadge
import com.alfan.fanllymusic.ui.theme.AccentPink
import com.alfan.fanllymusic.ui.theme.DarkSurfaceVariant
import com.alfan.fanllymusic.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    onDismiss: () -> Unit,
    viewModel: NowPlayingViewModel = hiltViewModel()
) {
    val song by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val position by viewModel.positionMs.collectAsState()
    val duration by viewModel.durationMs.collectAsState()
    val lyricsState by viewModel.lyricsState.collectAsState()
    val shuffleEnabled by viewModel.shuffleEnabled.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val sleepTimer by viewModel.sleepTimer.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val queueState by viewModel.queueState.collectAsState()
    val queueSongs by viewModel.queueSongs.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    val artworkAccentColor by viewModel.artworkAccentColor.collectAsState()
    val currentSong = song ?: return
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val compactHeight = configuration.screenHeightDp < 720
    val lrcPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        }.getOrNull()?.let { rawLrc ->
            viewModel.saveManualLyrics(rawLrc)
        }
    }
    var showLyrics by remember { mutableStateOf(false) }
    var showPlaybackMenu by remember { mutableStateOf(false) }
    var showSongInfo by remember { mutableStateOf(false) }
    var showEditSongInfo by remember { mutableStateOf(false) }
    var showPlaylistPicker by remember { mutableStateOf(false) }
    var showQueue by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var entered by remember(currentSong.id) { mutableStateOf(false) }
    LaunchedEffect(currentSong.id) {
        entered = true
    }
    val entryProgress by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(durationMillis = 360),
        label = "now_playing_entry"
    )
    val playScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.08f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "play_button_scale"
    )
    val artScale by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.98f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "album_art_scale"
    )
    val screenWidth = configuration.screenWidthDp
    val artworkSize = when {
        screenWidth < 360 -> 242.dp
        compactHeight -> 260.dp
        else -> 306.dp
    }
    val topGap = if (compactHeight) 28.dp else 74.dp
    val artworkToInfoGap = if (compactHeight) 36.dp else 58.dp
    val horizontalPadding = 24.dp
    val titleSize = if (compactHeight) 25.sp else 30.sp
    val dynamicAccent = Color(artworkAccentColor ?: 0xFFE91E63.toInt())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                enabled = false,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {}
    ) {
        BlurredBackground(albumArtUri = currentSong.albumArtUri)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Black.copy(alpha = 0.06f),
                        0.44f to Color.Black.copy(alpha = 0.18f),
                        1.0f to Color.Black.copy(alpha = 0.58f)
                    )
                )
        )

        if (!showLyrics) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .graphicsLayer {
                        alpha = entryProgress
                        translationY = (1f - entryProgress) * 26f
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.KeyboardArrowDown, "Dismiss", tint = White, modifier = Modifier.size(34.dp))
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.size(48.dp))
                }

                Spacer(modifier = Modifier.height(topGap))

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .graphicsLayer {
                            scaleX = 0.94f + (0.06f * entryProgress)
                            scaleY = 0.94f + (0.06f * entryProgress)
                            alpha = entryProgress
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(artworkSize + 12.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        White.copy(alpha = 0.10f),
                                        dynamicAccent.copy(alpha = 0.14f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    Image(
                        painter = rememberAsyncImagePainter(currentSong.albumArtUri),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(artworkSize)
                            .graphicsLayer {
                                scaleX = artScale
                                scaleY = artScale
                                shadowElevation = 28f
                            }
                            .clip(RoundedCornerShape(14.dp))
                            .background(DarkSurfaceVariant)
                    )
                }

                Spacer(modifier = Modifier.height(artworkToInfoGap))

                Column(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentSong.displayTitle.ifBlank { "Unknown Title" },
                                color = White.copy(alpha = 0.96f),
                                fontSize = titleSize,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentSong.displayArtist.ifBlank { "Unknown Artist" },
                                color = White.copy(alpha = 0.56f),
                                fontSize = if (compactHeight) 16.sp else 18.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = { viewModel.toggleFavorite() },
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(White.copy(alpha = 0.14f))
                        ) {
                            Icon(
                                imageVector = if (currentSong.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (currentSong.isFavorite) dynamicAccent else White.copy(alpha = 0.82f)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        IconButton(
                            onClick = { showPlaybackMenu = true },
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(White.copy(alpha = 0.14f))
                        ) {
                            Icon(Icons.Default.MoreVert, "Playback options", tint = White.copy(alpha = 0.82f))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    QualityBadge(quality = currentSong.quality)
                    if (sleepTimer != SleepTimerOption.Off) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Sleep timer: ${sleepTimer.label}",
                            color = White.copy(alpha = 0.48f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(if (compactHeight) 24.dp else 38.dp))

                    SlimProgressBar(
                        positionMs = position,
                        durationMs = duration,
                        activeColor = dynamicAccent.copy(alpha = 0.72f),
                        onSeekTo = viewModel::seekTo
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(formatTime(position), color = White.copy(alpha = 0.58f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text("-${formatTime(duration - position)}", color = White.copy(alpha = 0.58f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }

                    Spacer(modifier = Modifier.height(if (compactHeight) 12.dp else 18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.skipToPrevious() }, modifier = Modifier.size(72.dp)) {
                            Icon(Icons.Default.SkipPrevious, "Previous", tint = White, modifier = Modifier.size(54.dp))
                        }
                        Spacer(modifier = Modifier.width(26.dp))
                        IconButton(
                            onClick = { viewModel.togglePlayPause() },
                            modifier = Modifier
                                .size(if (compactHeight) 76.dp else 84.dp)
                                .graphicsLayer { scaleX = playScale; scaleY = playScale }
                                .clip(CircleShape)
                                .background(White)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = Color.Black,
                                modifier = Modifier.size(52.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(26.dp))
                        IconButton(onClick = { viewModel.skipToNext() }, modifier = Modifier.size(72.dp)) {
                            Icon(Icons.Default.SkipNext, "Next", tint = White, modifier = Modifier.size(54.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(if (compactHeight) 8.dp else 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showLyrics = true }, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Rounded.Lyrics, "Lyrics", tint = White.copy(alpha = 0.78f), modifier = Modifier.size(23.dp))
                        }
                        IconButton(onClick = { viewModel.setShuffleEnabled(!shuffleEnabled) }, modifier = Modifier.size(48.dp)) {
                            Icon(
                                Icons.Rounded.Shuffle,
                                "Shuffle",
                                tint = if (shuffleEnabled) dynamicAccent else White.copy(alpha = 0.56f),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        IconButton(onClick = { viewModel.cycleRepeatMode() }, modifier = Modifier.size(48.dp)) {
                            Icon(
                                Icons.Rounded.Repeat,
                                "Repeat",
                                tint = if (repeatMode != Player.REPEAT_MODE_OFF) dynamicAccent else White.copy(alpha = 0.56f),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        IconButton(onClick = { showPlaybackMenu = true }, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Rounded.QueueMusic, "Queue and options", tint = White.copy(alpha = 0.56f), modifier = Modifier.size(22.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(if (compactHeight) 10.dp else 18.dp))
                }
            }
        }

        AnimatedVisibility(
            visible = showLyrics,
            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn() + scaleIn(initialScale = 0.96f),
            exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut() + scaleOut(targetScale = 0.96f)
        ) {
            LyricsModeOverlay(
                songTitle = currentSong.displayTitle,
                lyricsState = lyricsState,
                positionMs = position,
                durationMs = duration,
                isPlaying = isPlaying,
                onDismiss = { showLyrics = false },
                onSearchOnline = viewModel::searchOnlineLyrics,
                onPickLyrics = { lrcPicker.launch("*/*") },
                onSeekTo = viewModel::seekTo,
                onTogglePlayPause = viewModel::togglePlayPause,
                onSkipPrevious = viewModel::skipToPrevious,
                onSkipNext = viewModel::skipToNext
            )
        }

        if (showPlaybackMenu) {
            PlaybackOptionsSheet(
                currentSong = currentSong,
                shuffleEnabled = shuffleEnabled,
                repeatMode = repeatMode,
                sleepTimer = sleepTimer,
                sheetState = sheetState,
                onDismiss = { showPlaybackMenu = false },
                onShuffleChange = viewModel::setShuffleEnabled,
                onRepeatModeChange = viewModel::setRepeatMode,
                onSleepTimerChange = viewModel::setSleepTimer,
                onSearchLyrics = viewModel::searchOnlineLyrics,
                onAddToPlaylist = {
                    showPlaybackMenu = false
                    showPlaylistPicker = true
                },
                onShowInfo = {
                    showPlaybackMenu = false
                    showSongInfo = true
                },
                onEditInfo = {
                    showPlaybackMenu = false
                    showEditSongInfo = true
                },
                onSelectLyrics = {
                    showPlaybackMenu = false
                    lrcPicker.launch("*/*")
                },
                onShowQueue = {
                    showPlaybackMenu = false
                    showQueue = true
                },
                onPlayNext = viewModel::playCurrentNext,
                onAddToQueue = viewModel::addCurrentToQueue,
                onOpenEqualizer = { openSystemEqualizer(context) }
            )
        }

        if (showSongInfo) {
            SongInfoSheet(
                currentSong = currentSong,
                sheetState = sheetState,
                onDismiss = { showSongInfo = false },
                onEdit = {
                    showSongInfo = false
                    showEditSongInfo = true
                }
            )
        }

        if (showEditSongInfo) {
            EditSongInfoSheet(
                currentSong = currentSong,
                sheetState = sheetState,
                onDismiss = { showEditSongInfo = false },
                onSave = { title, artist, album, genre, year, track ->
                    viewModel.saveMetadataOverride(title, artist, album, genre, year, track)
                    showEditSongInfo = false
                },
                onReset = {
                    viewModel.resetMetadataOverride()
                    showEditSongInfo = false
                }
            )
        }

        if (showQueue) {
            QueueSheet(
                queueSongs = queueSongs,
                recentlyPlayed = recentlyPlayed,
                currentIndex = queueState.currentIndex,
                sheetState = sheetState,
                onDismiss = { showQueue = false },
                onPlayAt = viewModel::skipToQueueIndex,
                onRemoveAt = viewModel::removeQueueItem,
                onClear = viewModel::clearQueueAfterCurrent
            )
        }

        if (showPlaylistPicker) {
            PlaylistPickerSheet(
                playlists = playlists,
                sheetState = sheetState,
                onDismiss = { showPlaylistPicker = false },
                onPlaylistSelected = { playlistId ->
                    viewModel.addCurrentSongToPlaylist(playlistId)
                    showPlaylistPicker = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaybackOptionsSheet(
    currentSong: Song,
    shuffleEnabled: Boolean,
    repeatMode: Int,
    sleepTimer: SleepTimerOption,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onShuffleChange: (Boolean) -> Unit,
    onRepeatModeChange: (Int) -> Unit,
    onSleepTimerChange: (SleepTimerOption) -> Unit,
    onSearchLyrics: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onShowInfo: () -> Unit,
    onEditInfo: () -> Unit,
    onSelectLyrics: () -> Unit,
    onShowQueue: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onOpenEqualizer: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF121214),
        contentColor = White,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = currentSong.displayTitle.ifBlank { "Current song" },
                color = White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = currentSong.displayArtist.ifBlank { "Unknown Artist" },
                color = White.copy(alpha = 0.56f),
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            HorizontalDivider(color = White.copy(alpha = 0.10f))

            MenuSwitchRow(
                icon = Icons.Rounded.Shuffle,
                title = "Shuffle",
                value = if (shuffleEnabled) "On" else "Off",
                selected = shuffleEnabled,
                onClick = { onShuffleChange(!shuffleEnabled) }
            )

            Text("Repeat", color = White.copy(alpha = 0.48f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                ChoicePill("Off", repeatMode == Player.REPEAT_MODE_OFF) {
                    onRepeatModeChange(Player.REPEAT_MODE_OFF)
                }
                ChoicePill("One", repeatMode == Player.REPEAT_MODE_ONE) {
                    onRepeatModeChange(Player.REPEAT_MODE_ONE)
                }
                ChoicePill("All", repeatMode == Player.REPEAT_MODE_ALL) {
                    onRepeatModeChange(Player.REPEAT_MODE_ALL)
                }
            }

            Text("Sleep Timer", color = White.copy(alpha = 0.48f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                SleepTimerOption.entries.forEach { option ->
                    ChoicePill(option.label, sleepTimer == option) {
                        onSleepTimerChange(option)
                    }
                }
            }

            HorizontalDivider(color = White.copy(alpha = 0.10f))

            MenuActionRow(Icons.Rounded.Lyrics, "Search lyrics", "Find online if no local lyrics exist", onClick = onSearchLyrics)
            MenuActionRow(Icons.Rounded.Lyrics, "Refresh lyrics", "Force LRCLIB re-search and update cache", onClick = onSearchLyrics)
            MenuActionRow(Icons.Rounded.Lyrics, "Select .lrc/.txt file", "Attach local lyrics to this song", onClick = onSelectLyrics)
            MenuActionRow(Icons.Rounded.QueueMusic, "Up Next", "View and manage current queue", onClick = onShowQueue)
            MenuActionRow(Icons.Rounded.QueueMusic, "Play next", "Place this song after current playback", onClick = onPlayNext)
            MenuActionRow(Icons.Rounded.QueueMusic, "Add to queue", "Append this song to Up Next", onClick = onAddToQueue)
            MenuActionRow(Icons.Rounded.PlaylistAdd, "Add to playlist", "Choose a playlist", onClick = onAddToPlaylist)
            MenuActionRow(Icons.Rounded.Info, "View song info", "Path, quality, duration", onClick = onShowInfo)
            MenuActionRow(Icons.Rounded.Info, "Edit song info", "Fix local title, artist, album, and more", onClick = onEditInfo)
            MenuActionRow(Icons.Rounded.Info, "Equalizer", "Open system audio effects", onClick = onOpenEqualizer)

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun MenuSwitchRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = if (selected) AccentPink else White.copy(alpha = 0.70f))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, color = White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Text(value, color = if (selected) AccentPink else White.copy(alpha = 0.50f), fontSize = 14.sp)
    }
}

@Composable
private fun MenuActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = White.copy(alpha = if (onClick != null) 0.72f else 0.34f))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = White.copy(alpha = if (onClick != null) 0.94f else 0.50f), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = White.copy(alpha = 0.38f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaylistPickerSheet(
    playlists: List<Playlist>,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onPlaylistSelected: (Long) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF121214),
        contentColor = White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Add to playlist", color = White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            if (playlists.isEmpty()) {
                Text(
                    "No playlists yet. Create a playlist from the Playlists tab first.",
                    color = White.copy(alpha = 0.56f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            } else {
                playlists.forEach { playlist ->
                    MenuActionRow(
                        icon = Icons.Rounded.PlaylistAdd,
                        title = playlist.name,
                        subtitle = "Add current song",
                        onClick = { onPlaylistSelected(playlist.id) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun ChoicePill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .height(38.dp)
            .widthIn(min = 64.dp)
            .clip(RoundedCornerShape(19.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(19.dp),
        color = if (selected) AccentPink.copy(alpha = 0.24f) else White.copy(alpha = 0.08f),
        contentColor = if (selected) AccentPink else White.copy(alpha = 0.70f)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 14.dp)) {
            Text(text, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QueueSheet(
    queueSongs: List<Song>,
    recentlyPlayed: List<Song>,
    currentIndex: Int,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onPlayAt: (Int) -> Unit,
    onRemoveAt: (Int) -> Unit,
    onClear: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF121214),
        contentColor = White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Up Next", color = White, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                TextButton(onClick = onClear) {
                    Text("Clear", color = White.copy(alpha = 0.62f))
                }
            }
            if (queueSongs.isEmpty()) {
                Text("Queue is empty.", color = White.copy(alpha = 0.56f), fontSize = 14.sp)
            } else {
                queueSongs.forEachIndexed { index, song ->
                    QueueSongRow(
                        song = song,
                        active = index == currentIndex,
                        onClick = { onPlayAt(index) },
                        onRemove = { onRemoveAt(index) }
                    )
                }
            }

            if (recentlyPlayed.isNotEmpty()) {
                HorizontalDivider(color = White.copy(alpha = 0.10f))
                Text("Recently Played", color = White.copy(alpha = 0.48f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                recentlyPlayed.take(5).forEach { song ->
                    QueueSongRow(song = song, active = false, onClick = {}, onRemove = null)
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun QueueSongRow(
    song: Song,
    active: Boolean,
    onClick: () -> Unit,
    onRemove: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (active) White.copy(alpha = 0.10f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(song.albumArtUri),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(DarkSurfaceVariant)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                song.displayTitle,
                color = if (active) White else White.copy(alpha = 0.88f),
                fontSize = 15.sp,
                fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                song.displayArtist,
                color = White.copy(alpha = 0.48f),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (onRemove != null) {
            TextButton(onClick = onRemove) {
                Text("Remove", color = White.copy(alpha = 0.52f), fontSize = 12.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SongInfoSheet(
    currentSong: Song,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onEdit: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF121214),
        contentColor = White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Song Info", color = White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            InfoRow("Title", currentSong.displayTitle.ifBlank { "Unknown Title" })
            InfoRow("Artist", currentSong.displayArtist.ifBlank { "Unknown Artist" })
            InfoRow("Album", currentSong.album.ifBlank { "Unknown Album" })
            currentSong.genre?.let { InfoRow("Genre", it) }
            currentSong.year?.let { InfoRow("Year", it.toString()) }
            currentSong.trackNumber?.let { InfoRow("Track", it.toString()) }
            InfoRow("Duration", formatTime(currentSong.durationMs))
            InfoRow("Quality", "${currentSong.sampleRate} Hz / ${currentSong.bitDepth}-bit")
            InfoRow("File size", formatFileSize(currentSong.fileSizeBytes))
            InfoRow("Override", if (currentSong.metadataOverridden) "Local override active" else "Using file metadata")
            InfoRow("File", currentSong.filePath.ifBlank { "Unknown path" })
            LyricsEmptyButton(text = "Edit Song Info", filled = true, onClick = onEdit)
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditSongInfoSheet(
    currentSong: Song,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String?, Int?, Int?) -> Unit,
    onReset: () -> Unit
) {
    var title by remember(currentSong.id, currentSong.title) { mutableStateOf(currentSong.displayTitle) }
    var artist by remember(currentSong.id, currentSong.artist) { mutableStateOf(currentSong.displayArtist) }
    var album by remember(currentSong.id, currentSong.album) { mutableStateOf(currentSong.album) }
    var genre by remember(currentSong.id, currentSong.genre) { mutableStateOf(currentSong.genre.orEmpty()) }
    var year by remember(currentSong.id, currentSong.year) { mutableStateOf(currentSong.year?.toString().orEmpty()) }
    var track by remember(currentSong.id, currentSong.trackNumber) { mutableStateOf(currentSong.trackNumber?.toString().orEmpty()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF121214),
        contentColor = White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Edit Song Info", color = White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            EditField("Title", title) { title = it }
            EditField("Artist", artist) { artist = it }
            EditField("Album", album) { album = it }
            EditField("Genre", genre) { genre = it }
            EditField("Year", year) { year = it.filter(Char::isDigit).take(4) }
            EditField("Track", track) { track = it.filter(Char::isDigit).take(3) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onReset) {
                    Text("Reset to file metadata", color = White.copy(alpha = 0.62f))
                }
                LyricsEmptyButton(
                    text = "Save",
                    filled = true,
                    onClick = {
                        onSave(
                            title,
                            artist,
                            album,
                            genre.takeIf { it.isNotBlank() },
                            year.toIntOrNull(),
                            track.toIntOrNull()
                        )
                    }
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun EditField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, color = White.copy(alpha = 0.42f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(value, color = White.copy(alpha = 0.88f), fontSize = 14.sp, lineHeight = 19.sp)
    }
}

@Composable
private fun LyricsModeOverlay(
    songTitle: String,
    lyricsState: LyricsState,
    positionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    onDismiss: () -> Unit,
    onSearchOnline: () -> Unit,
    onPickLyrics: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.64f))
            .clickable(
                enabled = false,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {}
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Black.copy(alpha = 0.58f),
                        0.22f to Color.Black.copy(alpha = 0.30f),
                        0.66f to Color.Black.copy(alpha = 0.34f),
                        1.0f to Color.Black.copy(alpha = 0.94f)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Close Lyrics",
                        tint = White.copy(alpha = 0.92f),
                        modifier = Modifier.size(30.dp)
                    )
                }

                Text(
                    text = songTitle.ifBlank { "Lyrics" },
                    color = White.copy(alpha = 0.78f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp)
                )

                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Rounded.QueueMusic,
                        contentDescription = "Queue",
                        tint = White.copy(alpha = 0.72f)
                    )
                }
            }

            when (lyricsState) {
                LyricsState.Idle -> LyricsLoadingState(
                    modifier = Modifier.weight(1f),
                    text = "Preparing lyrics"
                )
                LyricsState.LoadingLocal -> LyricsLoadingState(
                    modifier = Modifier.weight(1f),
                    text = "Looking for local lyrics"
                )
                LyricsState.LoadingOnline -> LyricsLoadingState(
                    modifier = Modifier.weight(1f),
                    text = "Searching online lyrics"
                )
                LyricsState.NotFound -> LyricsNotFoundState(
                    modifier = Modifier.weight(1f),
                    onSearchOnline = onSearchOnline,
                    onPickLyrics = onPickLyrics
                )
                is LyricsState.Error -> LyricsNotFoundState(
                    modifier = Modifier.weight(1f),
                    title = "Lyrics search failed",
                    subtitle = "Try online search again or select a .lrc/.txt file.",
                    onSearchOnline = onSearchOnline,
                    onPickLyrics = onPickLyrics
                )
                is LyricsState.SyncedLyricsFound -> {
                    Box(modifier = Modifier.weight(1f)) {
                        LyricsView(
                            lyrics = lyricsState.lines,
                            currentPositionMs = positionMs,
                            onSeekTo = onSeekTo
                        )
                    }
                }
                is LyricsState.UnsyncedLyricsFound -> {
                    Box(modifier = Modifier.weight(1f)) {
                        LyricsView(
                            lyrics = lyricsState.lines,
                            currentPositionMs = positionMs,
                            onSeekTo = onSeekTo
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color.Transparent,
                            0.18f to Color.Black.copy(alpha = 0.62f),
                            1.0f to Color.Black.copy(alpha = 0.94f)
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                SlimProgressBar(
                    positionMs = positionMs,
                    durationMs = durationMs,
                    activeColor = White.copy(alpha = 0.86f),
                    onSeekTo = onSeekTo
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(formatTime(positionMs), color = White.copy(alpha = 0.62f), fontSize = 11.sp)
                    Text("-${formatTime(durationMs - positionMs)}", color = White.copy(alpha = 0.62f), fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onSkipPrevious, modifier = Modifier.size(56.dp)) {
                        Icon(Icons.Default.SkipPrevious, "Previous", tint = White, modifier = Modifier.size(34.dp))
                    }
                    IconButton(
                        onClick = onTogglePlayPause,
                        modifier = Modifier
                            .size(66.dp)
                            .clip(CircleShape)
                            .background(White)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.Black,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    IconButton(onClick = onSkipNext, modifier = Modifier.size(56.dp)) {
                        Icon(Icons.Default.SkipNext, "Next", tint = White, modifier = Modifier.size(34.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LyricsLoadingState(
    modifier: Modifier = Modifier,
    text: String
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = White.copy(alpha = 0.82f),
            strokeWidth = 2.dp,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = text,
            color = White.copy(alpha = 0.72f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun LyricsNotFoundState(
    modifier: Modifier = Modifier,
    title: String = "No lyrics found",
    subtitle: String = "Search online or select a .lrc/.txt file.",
    onSearchOnline: () -> Unit,
    onPickLyrics: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(White.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Lyrics,
                contentDescription = null,
                tint = White.copy(alpha = 0.68f),
                modifier = Modifier.size(38.dp)
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(title, color = White, fontWeight = FontWeight.ExtraBold, fontSize = 21.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            subtitle,
            color = White.copy(alpha = 0.58f),
            fontSize = 14.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(18.dp))
        LyricsEmptyButton(text = "Search online lyrics", filled = true, onClick = onSearchOnline)
        Spacer(modifier = Modifier.height(10.dp))
        LyricsEmptyButton(text = "Select .lrc/.txt file", filled = false, onClick = onPickLyrics)
    }
}

@Composable
private fun LyricsEmptyButton(
    text: String,
    filled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .height(42.dp)
            .widthIn(min = 190.dp)
            .clip(RoundedCornerShape(21.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(21.dp),
        color = if (filled) White.copy(alpha = 0.92f) else White.copy(alpha = 0.10f),
        contentColor = if (filled) Color.Black else White.copy(alpha = 0.86f)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 18.dp)) {
            Text(text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SlimProgressBar(
    positionMs: Long,
    durationMs: Long,
    activeColor: Color,
    onSeekTo: (Long) -> Unit
) {
    var widthPx by remember { mutableIntStateOf(0) }
    var isDragging by remember { mutableStateOf(false) }
    val safeDuration = durationMs.coerceAtLeast(1L)
    val progress = (positionMs.toFloat() / safeDuration.toFloat()).coerceIn(0f, 1f)
    val thumbAlpha by animateFloatAsState(
        targetValue = if (isDragging) 1f else 0f,
        animationSpec = tween(durationMillis = 120),
        label = "progress_thumb_alpha"
    )

    fun seekFromX(x: Float) {
        if (widthPx <= 0) return
        val target = ((x / widthPx.toFloat()).coerceIn(0f, 1f) * safeDuration).toLong()
        onSeekTo(target)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .onSizeChanged { widthPx = it.width }
            .pointerInput(safeDuration, widthPx) {
                detectTapGestures { offset -> seekFromX(offset.x) }
            }
            .pointerInput(safeDuration, widthPx) {
                detectDragGestures(
                    onDragStart = {
                        isDragging = true
                        seekFromX(it.x)
                    },
                    onDragEnd = { isDragging = false },
                    onDragCancel = { isDragging = false }
                ) { change, _ ->
                    seekFromX(change.position.x)
                    change.consume()
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.5.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(White.copy(alpha = 0.22f))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .height(4.5.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(activeColor)
        )
        if (progress > 0f && thumbAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(34.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .graphicsLayer { alpha = thumbAlpha }
                        .clip(CircleShape)
                        .background(White.copy(alpha = 0.95f))
                )
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    if (ms < 0) return "0:00"
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format("%d:%02d", min, sec)
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0L) return "Unknown"
    val mb = bytes / (1024f * 1024f)
    return String.format("%.1f MB", mb)
}

private fun openSystemEqualizer(context: Context) {
    val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
        .putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, "No system equalizer available on this device.", Toast.LENGTH_SHORT).show()
    }
}
