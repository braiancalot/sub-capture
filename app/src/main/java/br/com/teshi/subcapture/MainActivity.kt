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

class MainActivity : ComponentActivity() {
    private val videoPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { OverlayService.start(this) }

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
        if (checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
            videoPermissionLauncher.launch(Manifest.permission.READ_MEDIA_VIDEO)
            return
        }
        OverlayService.start(this)
    }

    private fun copyToClipboard(text: String) {
        val clipboardManager = getSystemService(ClipboardManager::class.java)
        clipboardManager.setPrimaryClip(ClipData.newPlainText("subtitle", text))
    }
}
