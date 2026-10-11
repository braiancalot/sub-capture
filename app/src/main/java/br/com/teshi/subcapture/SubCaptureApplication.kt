package br.com.teshi.subcapture

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.provider.Settings
import android.widget.Toast
import java.io.File

class SubCaptureApplication : Application() {
    val sentenceStore: SentenceStore by lazy { SentenceStore(File(filesDir, "captures.json")) }
}

val Context.sentenceStore: SentenceStore
    get() = (applicationContext as SubCaptureApplication).sentenceStore

val Context.canShowOverlay: Boolean
    get() = Settings.canDrawOverlays(this)

val Context.canReadVideos: Boolean
    get() = checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED

val Context.isDebuggableBuild: Boolean
    get() = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
