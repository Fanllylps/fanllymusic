package com.alfan.fanllymusic.media

import android.os.CountDownTimer
import androidx.media3.common.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SleepTimerManager @Inject constructor(
    private val mediaController: FanllyMediaController
) {
    private var countDownTimer: CountDownTimer? = null
    private var fadeTimer: CountDownTimer? = null
    private var originalVolume = 1.0f

    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive

    private val _remainingMs = MutableStateFlow(0L)
    val remainingMs: StateFlow<Long> = _remainingMs

    private val _mode = MutableStateFlow<SleepTimerMode>(SleepTimerMode.Off)
    val mode: StateFlow<SleepTimerMode> = _mode

    private var playerListener: Player.Listener? = null

    enum class SleepTimerMode(val label: String, val minutes: Int? = null) {
        Off("Off"),
        Minutes15("15 min", 15),
        Minutes30("30 min", 30),
        Minutes60("60 min", 60),
        EndOfSong("End of song"),
        EndOfQueue("End of queue");

        companion object {
            fun fromString(s: String): SleepTimerMode {
                return entries.find {
                    when {
                        it.minutes != null -> "${it.minutes}" == s
                        else -> it.label == s
                    }
                } ?: Off
            }
        }
    }

    fun start(mode: SleepTimerMode) {
        cancel()
        _mode.value = mode
        if (mode == SleepTimerMode.Off) {
            _isActive.value = false
            _remainingMs.value = 0L
            return
        }
        _isActive.value = true
        when {
            mode.minutes != null -> startTimeTimer(mode.minutes)
            mode == SleepTimerMode.EndOfSong -> startEndOfSong()
            mode == SleepTimerMode.EndOfQueue -> startEndOfQueue()
        }
    }

    private fun startTimeTimer(minutes: Int) {
        val totalMs = minutes * 60 * 1000L
        _remainingMs.value = totalMs
        countDownTimer = object : CountDownTimer(totalMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _remainingMs.value = millisUntilFinished
            }
            override fun onFinish() {
                _remainingMs.value = 0
                mediaController.pause()
                _isActive.value = false
                _mode.value = SleepTimerMode.Off
            }
        }.start()
    }

    private fun startEndOfSong() {
        _remainingMs.value = -1
    }

    private fun startEndOfQueue() {
        _remainingMs.value = -1
    }

    fun onPlayerStoppedByEndOfSong() {
        if (_mode.value == SleepTimerMode.EndOfSong) {
            mediaController.pause()
            _isActive.value = false
            _mode.value = SleepTimerMode.Off
            _remainingMs.value = 0
        }
    }

    fun cancel() {
        countDownTimer?.cancel()
        countDownTimer = null
        fadeTimer?.cancel()
        fadeTimer = null
        _isActive.value = false
        _remainingMs.value = 0L
    }
}