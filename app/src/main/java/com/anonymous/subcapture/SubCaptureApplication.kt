package com.anonymous.subcapture

import android.app.Application
import android.content.Context
import android.widget.Toast
import java.io.File

class SubCaptureApplication : Application() {
    val sentenceStore: SentenceStore by lazy { SentenceStore(File(filesDir, "sentences.txt")) }
}

val Context.sentenceStore: SentenceStore
    get() = (applicationContext as SubCaptureApplication).sentenceStore

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
