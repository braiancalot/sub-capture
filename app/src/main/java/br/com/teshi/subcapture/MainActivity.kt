package br.com.teshi.subcapture

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

private val runtimePermissions = listOf(
    Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.READ_MEDIA_VIDEO
)

class MainActivity : ComponentActivity() {
    // Without POST_NOTIFICATIONS Android drops the service's toasts while another app is in front
    private val runtimePermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { OverlayService.start(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(SystemBarStyle.dark(Color.TRANSPARENT), SystemBarStyle.dark(Color.TRANSPARENT))
        setContent {
            val sentences by sentenceStore.sentences.collectAsState()
            val isOverlayRunning by OverlayService.isRunning.collectAsState()
            SubCaptureTheme {
                SentenceListScreen(
                    sentences = sentences,
                    isOverlayRunning = isOverlayRunning,
                    onToggleCapture = { if (isOverlayRunning) OverlayService.stop(this) else requestCapture() },
                    onDeleteSentence = sentenceStore::removeAt,
                    onCopyText = ::copyToClipboard,
                )
            }
        }
    }

    private fun requestCapture() {
        if (!Settings.canDrawOverlays(this)) {
            showToast("Permita sobrepor a outros apps e toque de novo")
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            return
        }
        if (!hasVlcSessionAccess()) {
            showToast("Permita o acesso a notificações e toque de novo")
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            return
        }
        val missingPermissions = runtimePermissions.filter {
            checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isNotEmpty()) {
            runtimePermissionsLauncher.launch(missingPermissions.toTypedArray())
            return
        }
        OverlayService.start(this)
    }

    private fun copyToClipboard(text: String) {
        val clipboardManager = getSystemService(ClipboardManager::class.java)
        clipboardManager.setPrimaryClip(ClipData.newPlainText("subtitle", text))
    }
}
