package com.alfan.fanllymusic.util

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import javax.inject.Inject

data class EmbeddedLyrics(
    val rawLyrics: String,
    val fieldName: String
)

class EmbeddedLyricsReader @Inject constructor() {
    fun read(file: File): EmbeddedLyrics? {
        if (!file.isFile) return null
        return when (file.extension.lowercase()) {
            "flac" -> readFlacVorbisComments(file)
            "mp3" -> readId3Lyrics(file)
            else -> null
        }
    }

    private fun readFlacVorbisComments(file: File): EmbeddedLyrics? = runCatching {
        file.inputStream().buffered().use { input ->
            val signature = input.readNBytesCompat(4)
            if (signature.decodeToString() != "fLaC") return@runCatching null

            var isLast = false
            while (!isLast) {
                val header = input.readNBytesCompat(4)
                if (header.size < 4) return@runCatching null
                isLast = (header[0].toInt() and 0x80) != 0
                val blockType = header[0].toInt() and 0x7F
                val blockLength = ((header[1].toInt() and 0xFF) shl 16) or
                    ((header[2].toInt() and 0xFF) shl 8) or
                    (header[3].toInt() and 0xFF)
                val block = input.readNBytesCompat(blockLength)
                if (block.size < blockLength) return@runCatching null
                if (blockType == FLAC_VORBIS_COMMENT_BLOCK) {
                    return@runCatching chooseVorbisLyrics(parseVorbisComments(block))
                }
            }
            null
        }
    }.getOrNull()

    private fun parseVorbisComments(block: ByteArray): Map<String, List<String>> {
        val buffer = ByteBuffer.wrap(block).order(ByteOrder.LITTLE_ENDIAN)
        if (buffer.remaining() < 8) return emptyMap()
        val vendorLength = buffer.int.takeIf { it >= 0 && it <= buffer.remaining() } ?: return emptyMap()
        buffer.position(buffer.position() + vendorLength)
        if (buffer.remaining() < 4) return emptyMap()

        val commentCount = buffer.int
        val comments = linkedMapOf<String, MutableList<String>>()
        repeat(commentCount.coerceAtLeast(0)) {
            if (buffer.remaining() < 4) return@repeat
            val length = buffer.int
            if (length < 0 || length > buffer.remaining()) return@repeat
            val rawComment = ByteArray(length)
            buffer.get(rawComment)
            val comment = rawComment.toString(Charsets.UTF_8)
            val separator = comment.indexOf('=')
            if (separator > 0) {
                val key = comment.substring(0, separator).trim().uppercase()
                val value = comment.substring(separator + 1).trim()
                if (value.isNotBlank()) {
                    comments.getOrPut(key) { mutableListOf() } += value
                }
            }
        }
        return comments
    }

    private fun chooseVorbisLyrics(comments: Map<String, List<String>>): EmbeddedLyrics? {
        for (field in SYNCED_FIELDS) {
            comments[field].orEmpty().firstOrNull { LyricsTextClassifier.hasSyncedTimestamp(it) }?.let {
                return EmbeddedLyrics(it, field)
            }
        }

        for (field in UNSYNCED_FIELDS) {
            comments[field].orEmpty().firstOrNull { LyricsTextClassifier.looksLikeLyrics(it) }?.let {
                return EmbeddedLyrics(it, field)
            }
        }

        comments["COMMENT"].orEmpty().firstOrNull { LyricsTextClassifier.looksLikeLyrics(it) }?.let {
            return EmbeddedLyrics(it, "COMMENT")
        }

        return null
    }

    private fun readId3Lyrics(file: File): EmbeddedLyrics? = runCatching {
        file.inputStream().buffered().use { input ->
            val header = input.readNBytesCompat(10)
            if (header.size < 10 || header[0] != 'I'.code.toByte() || header[1] != 'D'.code.toByte() || header[2] != '3'.code.toByte()) {
                return@runCatching null
            }
            val majorVersion = header[3].toInt()
            val tagSize = syncSafeInt(header, 6)
            val tagData = input.readNBytesCompat(tagSize)
            parseId3Frames(tagData, majorVersion)
        }
    }.getOrNull()

    private fun parseId3Frames(tagData: ByteArray, majorVersion: Int): EmbeddedLyrics? {
        val candidates = mutableListOf<EmbeddedLyrics>()
        var offset = 0
        while (offset + 10 <= tagData.size) {
            val frameId = tagData.copyOfRange(offset, offset + 4).toString(Charsets.ISO_8859_1)
            if (frameId.any { it.code == 0 }) break
            val frameSize = if (majorVersion == 4) syncSafeInt(tagData, offset + 4) else int32(tagData, offset + 4)
            if (frameSize <= 0 || offset + 10 + frameSize > tagData.size) break

            val frameData = tagData.copyOfRange(offset + 10, offset + 10 + frameSize)
            when (frameId) {
                "SYLT" -> parseSylt(frameData)?.let { candidates += EmbeddedLyrics(it, "SYLT") }
                "USLT" -> parseId3DescribedTextFrame(frameData, skipLanguage = true)?.let {
                    candidates += EmbeddedLyrics(it, "USLT")
                }
                "COMM" -> parseId3DescribedTextFrame(frameData, skipLanguage = true)
                    ?.takeIf { LyricsTextClassifier.looksLikeLyrics(it) }
                    ?.let { candidates += EmbeddedLyrics(it, "COMM") }
                "TXXX" -> parseId3DescribedTextFrame(frameData, skipLanguage = false)?.let { text ->
                    if (LyricsTextClassifier.looksLikeLyrics(text)) candidates += EmbeddedLyrics(text, "TXXX")
                }
            }
            offset += 10 + frameSize
        }

        return candidates.firstOrNull { LyricsTextClassifier.hasSyncedTimestamp(it.rawLyrics) }
            ?: candidates.firstOrNull()
    }

    private fun parseSylt(data: ByteArray): String? {
        if (data.size < 6) return null
        val encoding = data[0].toInt() and 0xFF
        val timestampFormat = data[4].toInt() and 0xFF
        if (timestampFormat != ID3_TIMESTAMP_MILLISECONDS) return null

        var offset = 6
        offset = skipEncodedTerminatedText(data, offset, encoding)
        val lines = mutableListOf<String>()
        while (offset < data.size) {
            val textStart = offset
            offset = skipEncodedTerminatedText(data, offset, encoding)
            if (offset + 4 > data.size) break
            val textBytes = data.copyOfRange(textStart, encodedTerminatorStart(data, textStart, encoding))
            val text = decodeId3Text(encoding, textBytes).trim()
            val timestampMs = int32(data, offset).toLong()
            offset += 4
            if (text.isNotBlank()) {
                lines += "[${formatLrcTimestamp(timestampMs)}]$text"
            }
        }
        return lines.takeIf { it.isNotEmpty() }?.joinToString("\n")
    }

    private fun parseId3DescribedTextFrame(data: ByteArray, skipLanguage: Boolean): String? {
        if (data.isEmpty()) return null
        val encoding = data[0].toInt() and 0xFF
        var offset = 1
        if (skipLanguage) offset += 3
        if (offset >= data.size) return null
        offset = skipEncodedTerminatedText(data, offset, encoding)
        if (offset >= data.size) return null
        return decodeId3Text(encoding, data.copyOfRange(offset, data.size)).trim().takeIf { it.isNotBlank() }
    }

    private fun skipEncodedTerminatedText(data: ByteArray, start: Int, encoding: Int): Int {
        val terminatorStart = encodedTerminatorStart(data, start, encoding)
        return (terminatorStart + if (isTwoByteEncoding(encoding)) 2 else 1).coerceAtMost(data.size)
    }

    private fun encodedTerminatorStart(data: ByteArray, start: Int, encoding: Int): Int {
        var index = start.coerceAtLeast(0)
        return if (isTwoByteEncoding(encoding)) {
            while (index + 1 < data.size) {
                if (data[index] == 0.toByte() && data[index + 1] == 0.toByte()) return index
                index += 2
            }
            data.size
        } else {
            while (index < data.size) {
                if (data[index] == 0.toByte()) return index
                index++
            }
            data.size
        }
    }

    private fun decodeId3Text(encoding: Int, bytes: ByteArray): String {
        val charset: Charset = when (encoding) {
            1 -> Charsets.UTF_16
            2 -> Charset.forName("UTF-16BE")
            3 -> Charsets.UTF_8
            else -> Charsets.ISO_8859_1
        }
        return bytes.toString(charset).trim('\uFEFF', '\u0000')
    }

    private fun formatLrcTimestamp(timestampMs: Long): String {
        val minutes = timestampMs / 60_000
        val seconds = (timestampMs % 60_000) / 1000
        val millis = timestampMs % 1000
        return "%02d:%02d.%03d".format(minutes, seconds, millis)
    }

    private fun syncSafeInt(data: ByteArray, offset: Int): Int =
        ((data[offset].toInt() and 0x7F) shl 21) or
            ((data[offset + 1].toInt() and 0x7F) shl 14) or
            ((data[offset + 2].toInt() and 0x7F) shl 7) or
            (data[offset + 3].toInt() and 0x7F)

    private fun int32(data: ByteArray, offset: Int): Int =
        ((data[offset].toInt() and 0xFF) shl 24) or
            ((data[offset + 1].toInt() and 0xFF) shl 16) or
            ((data[offset + 2].toInt() and 0xFF) shl 8) or
            (data[offset + 3].toInt() and 0xFF)

    private fun isTwoByteEncoding(encoding: Int): Boolean = encoding == 1 || encoding == 2

    private fun java.io.InputStream.readNBytesCompat(length: Int): ByteArray {
        if (length <= 0) return ByteArray(0)
        val output = ByteArray(length)
        var read = 0
        while (read < length) {
            val count = read(output, read, length - read)
            if (count < 0) break
            read += count
        }
        return if (read == length) output else output.copyOf(read)
    }

    companion object {
        private const val FLAC_VORBIS_COMMENT_BLOCK = 4
        private const val ID3_TIMESTAMP_MILLISECONDS = 2
        private val SYNCED_FIELDS = listOf("SYNCEDLYRICS", "SYNCLYRICS", "LRC", "LYRICS", "UNSYNCEDLYRICS", "DESCRIPTION")
        private val UNSYNCED_FIELDS = listOf("LYRICS", "UNSYNCEDLYRICS", "DESCRIPTION")
    }
}
