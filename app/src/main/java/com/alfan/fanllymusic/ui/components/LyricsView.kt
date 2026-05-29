package com.alfan.fanllymusic.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alfan.fanllymusic.domain.model.LyricLine

private val ACTIVE_COLOR    = Color.White
private val INACTIVE_COLOR  = Color.White.copy(alpha = 0.38f)
private val PAST_COLOR      = Color.White.copy(alpha = 0.28f)

private const val ANIM_MS = 420

/**
 * Apple Music–style lyrics view.
 *
 * - Synced: active line follows currentPositionMs, auto-scrolls when index changes.
 * - Unsynced: all lines shown at equal dim alpha; no scroll or active state.
 *   Detected by timestampMs == -1L on all lines (set by LrcParser plain-text fallback).
 * - Tap on a synced line → seeks to that timestamp.
 */
@Composable
fun LyricsView(
    lyrics: List<LyricLine>,
    currentPositionMs: Long,
    onSeekTo: (Long) -> Unit
) {
    if (lyrics.isEmpty()) return

    val isUnsynced = remember(lyrics) { lyrics.all { it.timestampMs < 0L } }

    if (isUnsynced) {
        UnsyncedLyricsView(lyrics = lyrics)
    } else {
        SyncedLyricsView(
            lyrics = lyrics,
            currentPositionMs = currentPositionMs,
            onSeekTo = onSeekTo
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Synced
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SyncedLyricsView(
    lyrics: List<LyricLine>,
    currentPositionMs: Long,
    onSeekTo: (Long) -> Unit
) {
    val listState = rememberLazyListState()
    val density = LocalDensity.current

    val activeIndexFromPosition = remember(lyrics, currentPositionMs) {
        val next = lyrics.indexOfFirst { it.timestampMs > currentPositionMs }
        if (next == -1) lyrics.lastIndex else (next - 1).coerceAtLeast(0)
    }
    val focusOffsetPx = remember(density) {
        with(density) { (-112).dp.roundToPx() }
    }

    // Scroll only when the active line index actually changes — not every 300ms tick.
    LaunchedEffect(activeIndexFromPosition) {
        if (activeIndexFromPosition >= 0 && lyrics.isNotEmpty()) {
            listState.animateScrollToItem(
                index = (activeIndexFromPosition - 1).coerceAtLeast(0),
                scrollOffset = focusOffsetPx
            )
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start  = 28.dp,
            end    = 28.dp,
            top    = 138.dp,
            bottom = 320.dp
        )
    ) {
        itemsIndexed(
            items = lyrics,
            key   = { _, line -> "${line.timestampMs}_${line.text}" }
        ) { index, line ->

            val state = when {
                index == activeIndexFromPosition -> LyricState.ACTIVE
                index < activeIndexFromPosition  -> LyricState.PAST
                else                             -> LyricState.NEXT
            }

            val color = when (state) {
                LyricState.ACTIVE -> ACTIVE_COLOR
                LyricState.PAST   -> PAST_COLOR
                LyricState.NEXT   -> INACTIVE_COLOR
            }

            val scale by animateFloatAsState(
                targetValue = if (state == LyricState.ACTIVE) 1.075f else 1f,
                animationSpec = tween(ANIM_MS),
                label = "lyric_scale_$index"
            )
            
            val alpha = when (state) {
                LyricState.ACTIVE -> 1f
                LyricState.NEXT -> 0.92f
                LyricState.PAST -> 0.78f
            }

            val verticalPadding = if (state == LyricState.ACTIVE) 18.dp else 13.dp

            val fontSize = when (state) {
                LyricState.ACTIVE -> 39.sp
                else              -> 33.sp
            }
            val fontWeight = when (state) {
                LyricState.ACTIVE -> FontWeight.ExtraBold
                LyricState.NEXT   -> FontWeight.SemiBold
                LyricState.PAST   -> FontWeight.Medium
            }

            Text(
                text = line.text,
                color = color,
                fontSize = fontSize,
                fontWeight = fontWeight,
                fontFamily = FontFamily.SansSerif,
                lineHeight = when (state) {
                    LyricState.ACTIVE -> 50.sp
                    else              -> 43.sp
                },
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        this.alpha = alpha
                        scaleX = scale
                        scaleY = scale
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0.5f)
                    }
                    .padding(vertical = verticalPadding)
                    .pointerInput(line.timestampMs) {
                        detectTapGestures {
                            onSeekTo(line.timestampMs)
                        }
                    }
            )
        }
    }

    // Gradient scrim: top and bottom edges fade to reveal depth; never covers text.
    LyricGradientEdges()
}

// ─────────────────────────────────────────────────────────────────────────────
// Unsynced
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun UnsyncedLyricsView(lyrics: List<LyricLine>) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start  = 28.dp,
                end    = 28.dp,
                top    = 92.dp,
                bottom = 300.dp
            )
        ) {
            item {
                Text(
                    text = "Unsynced Lyrics",
                    color = Color.White.copy(alpha = 0.35f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 1.4.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            itemsIndexed(
                items = lyrics,
                key   = { _, line -> line.text }
            ) { _, line ->
                Text(
                    text = line.text,
                    color = Color.White.copy(alpha = 0.76f),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    lineHeight = 45.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                )
            }
        }

        LyricGradientEdges()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LyricGradientEdges() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0.00f to Color.Black.copy(alpha = 0.80f),
                    0.13f to Color.Transparent,
                    0.70f to Color.Transparent,
                    1.00f to Color.Black.copy(alpha = 0.94f)
                )
            )
    )
}

private enum class LyricState { ACTIVE, PAST, NEXT }
