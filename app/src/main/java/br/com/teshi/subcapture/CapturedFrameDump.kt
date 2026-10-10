package br.com.teshi.subcapture

import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val frameNameFormat = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS")

val Context.isDebuggableBuild: Boolean
    get() = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

fun Context.saveCapturedFrame(frame: Bitmap): File {
    val framesDirectory = File(getExternalFilesDir(null), "frames").apply { mkdirs() }
    val frameFile = File(framesDirectory, "frame-${LocalDateTime.now().format(frameNameFormat)}.png")
    frameFile.outputStream().use { frame.compress(Bitmap.CompressFormat.PNG, 100, it) }
    return frameFile
}
