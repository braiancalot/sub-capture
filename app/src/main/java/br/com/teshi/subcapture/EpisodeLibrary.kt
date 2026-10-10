package br.com.teshi.subcapture

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import java.io.FileInputStream
import java.io.IOException

private const val DURATION_TOLERANCE_MILLIS = 1000L

fun Context.videoFilesLasting(durationMillis: Long): List<VideoFileEntry> {
    val columns = arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.DISPLAY_NAME)
    val durationRange = arrayOf(
        "${durationMillis - DURATION_TOLERANCE_MILLIS}", "${durationMillis + DURATION_TOLERANCE_MILLIS}"
    )
    val rows = contentResolver.query(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, columns,
        "${MediaStore.Video.Media.DURATION} BETWEEN ? AND ?", durationRange, null
    ) ?: return emptyList()
    return rows.use {
        generateSequence { if (rows.moveToNext()) VideoFileEntry(rows.getLong(0), rows.getString(1)) else null }
            .toList()
    }
}

fun Context.readSubtitleCuesOf(videoFile: VideoFileEntry): List<SubtitleCue> {
    val videoUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoFile.mediaStoreId)
    val descriptor = contentResolver.openFileDescriptor(videoUri, "r")
        ?: throw IOException("MediaStore gave no file descriptor for $videoUri")
    return descriptor.use { FileInputStream(it.fileDescriptor).channel.use(::readSubtitleCues) }
}
