package br.com.teshi.subcapture

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val videoPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { OverlayService.start(this) }

    private val isSendingToSpreadsheet = mutableStateOf(false)
    private val isShowingSettings = mutableStateOf(false)
    private val isVlcShortcutOnHomeScreen = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(SystemBarStyle.dark(Color.TRANSPARENT), SystemBarStyle.dark(Color.TRANSPARENT))
        setContent { SubCaptureTheme { CurrentScreen() } }
        publishWatchInVlcShortcut()
    }

    // The shortcut is pinned from the launcher's own dialog, so the state is read again on return
    override fun onResume() {
        super.onResume()
        isVlcShortcutOnHomeScreen.value = isVlcShortcutPinned
    }

    @Composable
    private fun CurrentScreen() {
        if (isShowingSettings.value) SettingsScreenOfApp() else SentenceListScreenOfApp()
    }

    @Composable
    private fun SettingsScreenOfApp() {
        SettingsScreen(
            savedScriptAddress = spreadsheetScriptAddress.orEmpty(),
            onSaveScriptAddress = { spreadsheetScriptAddress = it },
            isVlcShortcutPinned = isVlcShortcutOnHomeScreen.value,
            onAddVlcShortcut = ::requestVlcShortcutPin,
            onOpenShortcutPermission = if (hasXiaomiPermissionEditor) ::openXiaomiPermissionEditor else null,
            onBack = { isShowingSettings.value = false },
        )
    }

    @Composable
    private fun SentenceListScreenOfApp() {
        val sentences by sentenceStore.sentences.collectAsState()
        val isOverlayRunning by OverlayService.isRunning.collectAsState()
        SentenceListScreen(
            sentences = sentences,
            isOverlayRunning = isOverlayRunning,
            isSendingToSpreadsheet = isSendingToSpreadsheet.value,
            onToggleCapture = { if (isOverlayRunning) OverlayService.stop(this) else requestCapture() },
            onDeleteSentences = sentenceStore::removeAll,
            onToggleMarkedWord = sentenceStore::toggleMarkedWord,
            onSendSelection = ::sendToSpreadsheet,
            onOpenSettings = { isShowingSettings.value = true },
        )
    }

    private fun requestCapture() {
        if (!canShowOverlay) {
            showToast(getString(R.string.overlay_permission_needed))
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            return
        }
        if (!canReadVideos) {
            videoPermissionLauncher.launch(Manifest.permission.READ_MEDIA_VIDEO)
            return
        }
        OverlayService.start(this)
    }

    private fun sendToSpreadsheet(selectedIndices: Set<Int>) {
        val scriptAddress = spreadsheetScriptAddress
        if (scriptAddress == null) {
            showToast(getString(R.string.send_needs_script_address))
            isShowingSettings.value = true
            return
        }
        // The script takes seconds to answer: a second tap meanwhile would write the rows twice
        if (isSendingToSpreadsheet.value) return
        isSendingToSpreadsheet.value = true
        lifecycleScope.launch {
            try {
                sendSelectionToSpreadsheet(scriptAddress, selectedIndices)
            } finally {
                isSendingToSpreadsheet.value = false
            }
        }
    }
}
