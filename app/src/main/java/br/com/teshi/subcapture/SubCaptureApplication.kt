package br.com.teshi.subcapture

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.widget.Toast
import java.io.File

class SubCaptureApplication : Application() {
    val sentenceStore: SentenceStore by lazy { SentenceStore(File(filesDir, "sentences.json")) }
}

val Context.sentenceStore: SentenceStore
    get() = (applicationContext as SubCaptureApplication).sentenceStore

val Context.isDebuggableBuild: Boolean
    get() = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
