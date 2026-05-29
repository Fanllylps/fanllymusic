package com.alfan.fanllymusic.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.alfan.fanllymusic.ui.screen.nowplaying.NowPlayingViewModel
import com.alfan.fanllymusic.ui.theme.AccentPink
import com.alfan.fanllymusic.ui.theme.GlassBackground
import com.alfan.fanllymusic.ui.theme.GlassBorder
import com.alfan.fanllymusic.ui.theme.TextSecondary
import com.alfan.fanllymusic.ui.theme.White

@Composable
fun MiniPlayer(
    onExpand: () -> Unit,
    viewModel: NowPlayingViewModel = hiltViewModel()
) {
    val song by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val positionMs by viewModel.positionMs.collectAsState()
    val durationMs by viewModel.durationMs.collectAsState()

    AnimatedVisibility(
        visible = song != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        song?.let { currentSong ->
            val rotation = remember { Animatable(0f) }
            LaunchedEffect(isPlaying) {
                if (isPlaying) {
                    while (true) {
                        rotation.animateTo(
                            targetValue = rotation.value + 360f,
                            animationSpec = tween(18000, easing = LinearEasing)
                        )
                    }
                } else {
                    rotation.stop()
                }
            }

            val playerShape = RoundedCornerShape(24.dp)
            val progress = if (durationMs > 0L) {
                (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
            } else {
                0f
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .fillMaxWidth()
                    .height(64.dp)
                    .animateContentSize()
                    .shadow(
                        elevation = 14.dp,
                        shape = playerShape,
                        ambientColor = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.45f),
                        spotColor = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.55f)
                    )
                    .clip(playerShape)
                    .background(GlassBackground)
                    .border(1.dp, GlassBorder, playerShape)
                    .clickable { onExpand() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentSong.albumArtUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(currentSong.albumArtUri),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(44.dp)
                                .rotate(rotation.value)
                                .clip(RoundedCornerShape(14.dp))
                                .background(GlassBorder)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.MusicNote,
                            contentDescription = null,
                            tint = White.copy(alpha = 0.7f),
                            modifier = Modifier
                                .size(44.dp)
                                .rotate(rotation.value)
                                .clip(RoundedCornerShape(14.dp))
                                .background(GlassBorder)
                                .padding(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        AnimatedContent(
                            targetState = currentSong.title,
                            transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(180)) },
                            label = "mini_player_title"
                        ) { title ->
                            Text(
                                text = title,
                                color = White,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = currentSong.artist,
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = { viewModel.togglePlayPause() }) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = White
                        )
                    }
                    IconButton(onClick = { viewModel.skipToNext() }) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = White)
                    }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth(progress)
                        .height(2.dp)
                        .background(AccentPink.copy(alpha = 0.86f))
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth(0.86f)
                        .height(1.dp)
                        .background(White.copy(alpha = 0.12f))
                )
            }
        }
    }
}
