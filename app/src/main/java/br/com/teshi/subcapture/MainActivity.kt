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

    private val isAskingScriptAddress = mutableStateOf(false)
    private val isSendingToSpreadsheet = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(SystemBarStyle.dark(Color.TRANSPARENT), SystemBarStyle.dark(Color.TRANSPARENT))
        setContent { SubCaptureTheme { CaptureListAndAddressDialog() } }
    }

    @Composable
    private fun CaptureListAndAddressDialog() {
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
            onChangeScriptAddress = { isAskingScriptAddress.value = true },
        )
        if (!isAskingScriptAddress.value) return
        ScriptAddressDialog(spreadsheetScriptAddress.orEmpty(), ::saveScriptAddress) {
            isAskingScriptAddress.value = false
        }
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

    private fun saveScriptAddress(scriptAddress: String) {
        spreadsheetScriptAddress = scriptAddress
        isAskingScriptAddress.value = false
    }

    private fun sendToSpreadsheet(selectedIndices: Set<Int>) {
        val scriptAddress = spreadsheetScriptAddress
        if (scriptAddress == null) {
            isAskingScriptAddress.value = true
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
