package br.com.teshi.subcapture

import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.SeekableByteChannel

private const val EBML_HEADER = 0x1A45DFA3L
private const val SEGMENT = 0x18538067L
private const val SEGMENT_INFO = 0x1549A966L
private const val TIMESTAMP_SCALE = 0x2AD7B1L
private const val TRACKS = 0x1654AE6BL
private const val TRACK_ENTRY = 0xAEL
private const val TRACK_NUMBER = 0xD7L
private const val TRACK_TYPE = 0x83L
private const val CODEC_ID = 0x86L
private const val CONTENT_ENCODINGS = 0x6D80L
private const val CLUSTER = 0x1F43B675L
private const val CLUSTER_TIMESTAMP = 0xE7L
private const val BLOCK_GROUP = 0xA0L
private const val BLOCK = 0xA1L
private const val BLOCK_DURATION = 0x9BL

private const val SUBTITLE_TRACK_TYPE = 0x11L
private const val UNKNOWN_SIZE = -1L
private const val NANOS_PER_MILLI = 1_000_000L
private const val CURSOR_BUFFER_BYTES = 8192
private val descendedMasters = setOf(SEGMENT, SEGMENT_INFO, TRACKS, TRACK_ENTRY, CLUSTER, BLOCK_GROUP)
private val textCodecIds = setOf(SUBRIP_CODEC_ID, ASS_CODEC_ID)

fun readSubtitleCues(matroskaFile: SeekableByteChannel): List<SubtitleCue> =
    MatroskaSubtitleScan(ChannelCursor(matroskaFile)).scan()

private class MatroskaTrack(
    var number: Long = 0,
    var type: Long = 0,
    var codecId: String = "",
    var isEncoded: Boolean = false,
)

private class PendingBlock(val startTicks: Long, val payload: String, var durationTicks: Long = 0)

// Reads elements in file order without tracking nesting, so unknown-size masters need no special case
private class MatroskaSubtitleScan(private val cursor: ChannelCursor) {
    private val tracks = mutableListOf<MatroskaTrack>()
    private val cues = mutableListOf<SubtitleCue>()
    private var subtitleTrack: MatroskaTrack? = null
    private var timestampScaleNanos = NANOS_PER_MILLI
    private var clusterTimestamp = 0L
    private var pendingBlock: PendingBlock? = null

    fun scan(): List<SubtitleCue> {
        if (cursor.readElementId() != EBML_HEADER) {
            throw IOException("not a Matroska file: it does not start with an EBML header")
        }
        skipElement(EBML_HEADER, cursor.readElementSize())
        while (cursor.position < cursor.size) readElement()
        flushPendingBlock()
        return cues.sortedBy { it.startMillis }
    }

    private fun readElement() {
        val id = cursor.readElementId()
        val size = cursor.readElementSize()
        when (id) {
            in descendedMasters -> enterMaster(id)
            TIMESTAMP_SCALE -> timestampScaleNanos = cursor.readUnsigned(size)
            TRACK_NUMBER -> tracks.last().number = cursor.readUnsigned(size)
            TRACK_TYPE -> tracks.last().type = cursor.readUnsigned(size)
            CODEC_ID -> tracks.last().codecId = cursor.readText(size)
            CONTENT_ENCODINGS -> skipElement(id, size).also { tracks.last().isEncoded = true }
            CLUSTER_TIMESTAMP -> clusterTimestamp = cursor.readUnsigned(size)
            BLOCK -> readBlock(size)
            BLOCK_DURATION -> cursor.readUnsigned(size).let { pendingBlock?.durationTicks = it }
            else -> skipElement(id, size)
        }
    }

    private fun enterMaster(id: Long) {
        if (id == TRACK_ENTRY) tracks.add(MatroskaTrack())
        if (id == CLUSTER || id == BLOCK_GROUP) flushPendingBlock()
        if (id == CLUSTER && subtitleTrack == null) subtitleTrack = chooseSubtitleTrack()
    }

    private fun skipElement(id: Long, size: Long) {
        if (size == UNKNOWN_SIZE) {
            throw IOException(
                "element 0x${id.toString(16)} at offset ${cursor.position} declares no size, so it cannot be skipped"
            )
        }
        if (cursor.position + size > cursor.size) {
            throw IOException(
                "element 0x${id.toString(16)} at offset ${cursor.position} claims $size bytes, " +
                    "past the end of the ${cursor.size}-byte file"
            )
        }
        cursor.skip(size)
    }

    private fun chooseSubtitleTrack(): MatroskaTrack {
        val textTrack = tracks.firstOrNull { it.type == SUBTITLE_TRACK_TYPE && it.codecId in textCodecIds }
            ?: throw IOException("no text subtitle track; tracks found: ${tracks.map { it.codecId }}")
        if (textTrack.isEncoded) {
            throw IOException(
                "subtitle track ${textTrack.number} (${textTrack.codecId}) is compressed or encrypted, " +
                    "which this reader does not support"
            )
        }
        return textTrack
    }

    private fun readBlock(size: Long) {
        val blockEnd = cursor.position + size
        if (cursor.readVariableInt(keepMarker = false) != subtitleTrack?.number) {
            cursor.skip(blockEnd - cursor.position)
            return
        }
        val startTicks = clusterTimestamp + cursor.readSignedInt16()
        cursor.skip(1)
        pendingBlock = PendingBlock(startTicks, cursor.readText(blockEnd - cursor.position))
    }

    private fun flushPendingBlock() {
        val block = pendingBlock ?: return
        pendingBlock = null
        val text = cueTextFromPayload(subtitleTrack?.codecId.orEmpty(), block.payload)
        if (text.isEmpty()) return
        val endTicks = block.startTicks + block.durationTicks
        cues.add(SubtitleCue(ticksToMillis(block.startTicks), ticksToMillis(endTicks), text))
    }

    private fun ticksToMillis(ticks: Long): Long = ticks * timestampScaleNanos / NANOS_PER_MILLI
}

private class ChannelCursor(private val channel: SeekableByteChannel) {
    private val buffer: ByteBuffer = ByteBuffer.allocate(CURSOR_BUFFER_BYTES).apply { limit(0) }
    private var bufferStart = 0L
    val size: Long = channel.size()
    val position: Long get() = bufferStart + buffer.position()

    fun skip(byteCount: Long) {
        if (byteCount <= buffer.remaining()) {
            buffer.position(buffer.position() + byteCount.toInt())
            return
        }
        bufferStart = position + byteCount
        buffer.limit(0)
    }

    fun readElementId(): Long = readVariableInt(keepMarker = true)

    fun readElementSize(): Long {
        val sizeStart = position
        val size = readVariableInt(keepMarker = false)
        val allOnes = (1L shl (7 * (position - sizeStart).toInt())) - 1
        return if (size == allOnes) UNKNOWN_SIZE else size
    }

    fun readVariableInt(keepMarker: Boolean): Long {
        val firstByte = readByte()
        val length = Integer.numberOfLeadingZeros(firstByte) - 23
        if (length > 8) throw IOException("invalid EBML variable-length integer at offset ${position - 1}")
        var value = (if (keepMarker) firstByte else firstByte and (0xFF shr length)).toLong()
        repeat(length - 1) { value = (value shl 8) or readByte().toLong() }
        return value
    }

    fun readUnsigned(byteCount: Long): Long {
        var value = 0L
        repeat(byteCount.toInt()) { value = (value shl 8) or readByte().toLong() }
        return value
    }

    fun readSignedInt16(): Long = ((readByte() shl 8) or readByte()).toShort().toLong()

    fun readText(byteCount: Long): String =
        String(ByteArray(byteCount.toInt()) { readByte().toByte() }, Charsets.UTF_8)

    private fun readByte(): Int {
        if (!buffer.hasRemaining()) refill()
        return buffer.get().toInt() and 0xFF
    }

    private fun refill() {
        val refillStart = position
        channel.position(refillStart)
        buffer.clear()
        if (channel.read(buffer) <= 0) {
            buffer.limit(0)
            throw IOException("file ends in the middle of an element, at offset $refillStart")
        }
        buffer.flip()
        bufferStart = refillStart
    }
}
