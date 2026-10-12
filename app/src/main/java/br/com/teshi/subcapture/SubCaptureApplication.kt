package br.com.teshi.subcapture

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.provider.Settings
import android.widget.Toast
import java.io.File

private const val SPREADSHEET_PREFS_NAME = "spreadsheet"
private const val SCRIPT_ADDRESS_KEY = "script_address"

class SubCaptureApplication : Application() {
    val sentenceStore: SentenceStore by lazy { SentenceStore(File(filesDir, "captures.json")) }
}

val Context.sentenceStore: SentenceStore
    get() = (applicationContext as SubCaptureApplication).sentenceStore

var Context.spreadsheetScriptAddress: String?
    get() = spreadsheetPrefs.getString(SCRIPT_ADDRESS_KEY, null)
    set(scriptAddress) = spreadsheetPrefs.edit().putString(SCRIPT_ADDRESS_KEY, scriptAddress).apply()

private val Context.spreadsheetPrefs: SharedPreferences
    get() = getSharedPreferences(SPREADSHEET_PREFS_NAME, Context.MODE_PRIVATE)

val Context.canShowOverlay: Boolean
    get() = Settings.canDrawOverlays(this)

val Context.canReadVideos: Boolean
    get() = checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED

val Context.isDebuggableBuild: Boolean
    get() = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
