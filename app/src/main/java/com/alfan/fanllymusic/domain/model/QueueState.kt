package com.alfan.fanllymusic.domain.model

data class QueueState(
    val songIds: List<Long> = emptyList(),
    val currentIndex: Int = -1
) {
    val currentSongId: Long?
        get() = songIds.getOrNull(currentIndex)

    val hasQueue: Boolean
        get() = songIds.isNotEmpty()
}
