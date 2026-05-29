package com.alfan.fanllymusic.domain.model

sealed interface LyricsState {
    data object Idle : LyricsState
    data object LoadingLocal : LyricsState
    data object LoadingOnline : LyricsState
    data class SyncedLyricsFound(val lines: List<LyricLine>) : LyricsState
    data class UnsyncedLyricsFound(val lines: List<LyricLine>) : LyricsState
    data object NotFound : LyricsState
    data class Error(val message: String) : LyricsState
}

fun LyricsState.linesOrEmpty(): List<LyricLine> = when (this) {
    LyricsState.Idle -> emptyList()
    LyricsState.LoadingLocal -> emptyList()
    LyricsState.LoadingOnline -> emptyList()
    is LyricsState.SyncedLyricsFound -> lines
    is LyricsState.UnsyncedLyricsFound -> lines
    LyricsState.NotFound -> emptyList()
    is LyricsState.Error -> emptyList()
}
