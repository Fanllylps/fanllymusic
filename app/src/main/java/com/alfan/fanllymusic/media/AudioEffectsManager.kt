package com.alfan.fanllymusic.media

import android.content.Context
import android.content.Intent
import android.media.audiofx.BassBoost
import android.media.audiofx.Virtualizer
import android.provider.Settings
import androidx.media3.common.Player
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioEffectsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var currentAudioSession = 0
    private var bassBoostStrength = 0
    private var virtualizerStrength = VirtualizerStrength.OFF

    enum class VirtualizerStrength(val value: Int, val label: String) {
        OFF(0, "Off"),
        LOW(33, "Low"),
        MEDIUM(66, "Medium"),
        HIGH(100, "High");

        companion object {
            fun fromString(s: String): VirtualizerStrength {
                return entries.find { it.label == s } ?: OFF
            }
        }
    }

    val isBassBoostSupported: Boolean
        get() = try {
            BassBoost::class.java != null
        } catch (_: Throwable) {
            false
        }

    val isVirtualizerSupported: Boolean
        get() = try {
            Virtualizer::class.java != null
        } catch (_: Throwable) {
            false
        }

    fun attachToPlayer(player: Player) {
        if (currentAudioSession != 0) return
        val sessionId = try {
            val field = player.javaClass.getDeclaredMethod("getAudioSessionId")
            field.invoke(player) as? Int ?: 0
        } catch (_: Throwable) {
            0
        }
        if (sessionId == 0) return
        currentAudioSession = sessionId
        try {
            bassBoost = BassBoost(0, sessionId).apply {
                enabled = bassBoostStrength > 0
                if (bassBoostStrength > 0) {
                    setStrength(bassBoostStrength.toShort())
                }
            }
        } catch (_: Throwable) {
            bassBoost = null
        }
        try {
            virtualizer = Virtualizer(0, sessionId).apply {
                enabled = virtualizerStrength != VirtualizerStrength.OFF
                if (virtualizerStrength != VirtualizerStrength.OFF) {
                    setStrength(virtualizerStrength.value.toShort())
                }
            }
        } catch (_: Throwable) {
            virtualizer = null
        }
    }

    fun setBassBoost(strength: Float) {
        bassBoostStrength = strength.toInt().coerceIn(0, 100)
        try {
            bassBoost?.enabled = bassBoostStrength > 0
            if (bassBoostStrength > 0) {
                bassBoost?.setStrength(bassBoostStrength.toShort())
            }
        } catch (_: Throwable) {}
    }

    fun setVirtualizer(strength: VirtualizerStrength) {
        virtualizerStrength = strength
        try {
            virtualizer?.enabled = strength != VirtualizerStrength.OFF
            if (strength != VirtualizerStrength.OFF) {
                virtualizer?.setStrength(strength.value.toShort())
            }
        } catch (_: Throwable) {}
    }

    fun openSystemEqualizer(): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_SOUND_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            true
        } catch (_: Throwable) {
            false
        }
    }

    fun release() {
        try { bassBoost?.release() } catch (_: Throwable) {}
        try { virtualizer?.release() } catch (_: Throwable) {}
        bassBoost = null
        virtualizer = null
        currentAudioSession = 0
    }
}