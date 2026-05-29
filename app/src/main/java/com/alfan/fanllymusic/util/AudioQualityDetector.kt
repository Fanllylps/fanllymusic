package com.alfan.fanllymusic.util

import android.media.MediaExtractor
import android.media.MediaFormat
import javax.inject.Inject

class AudioQualityDetector @Inject constructor() {
    fun detect(filePath: String): Pair<Int, Int> {
        return runCatching {
            val extractor = MediaExtractor()
            extractor.setDataSource(filePath)
            var sampleRate = 44_100
            var bitDepth = 16
            for (index in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(index)
                val mime = format.getString(MediaFormat.KEY_MIME).orEmpty()
                if (mime.startsWith("audio/")) {
                    if (format.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
                        sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                    }
                    if (format.containsKey(MediaFormat.KEY_PCM_ENCODING)) {
                        bitDepth = when (format.getInteger(MediaFormat.KEY_PCM_ENCODING)) {
                            3 -> 24
                            4 -> 32
                            else -> 16
                        }
                    }
                    break
                }
            }
            extractor.release()
            sampleRate to bitDepth
        }.getOrDefault(44_100 to 16)
    }
}
