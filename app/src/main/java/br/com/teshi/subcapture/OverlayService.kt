package br.com.teshi.subcapture

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val LOG_TAG = "OverlayService"
private const val NOTIFICATION_CHANNEL_ID = "overlay_channel"
private const val NOTIFICATION_ID = 1

class OverlayService : Service() {
    companion object {
        private val runningState = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = runningState.asStateFlow()

        fun start(context: Context) {
            context.startForegroundService(Intent(context, OverlayService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, OverlayService::class.java))
        }
    }

    private val vlcSubtitleCapture = VlcSubtitleCapture(this)
    private val captureScope = MainScope()
    private var floatingCaptureButton: FloatingCaptureButton? = null
    private var isCapturing = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, buildNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        floatingCaptureButton = FloatingCaptureButton(this, ::captureSubtitle).also { it.show() }
        runningState.value = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_NOT_STICKY

    override fun onDestroy() {
        super.onDestroy()
        captureScope.cancel()
        floatingCaptureButton?.remove()
        runningState.value = false
    }

    private fun buildNotification(): Notification {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID, "SubCapture Overlay", NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        return Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("SubCapture")
            .setContentText("Toque no botão flutuante para capturar legendas")
            .setSmallIcon(R.drawable.subcapture_mark)
            .build()
    }

    private fun captureSubtitle() {
        if (isCapturing) return
        isCapturing = true
        floatingCaptureButton?.startPulse()
        captureScope.launch {
            try {
                val capture = vlcSubtitleCapture.subtitleOnScreen()
                if (isDebuggableBuild) Log.d(LOG_TAG, "capture: $capture")
                finishCaptureWithSubtitle(capture.subtitle)
            } catch (failure: SubtitleCaptureFailure) {
                finishCaptureWithError(failure.userMessage, failure.message.orEmpty())
            }
        }
    }

    private fun finishCaptureWithSubtitle(subtitle: String?) {
        finishCapture()
        if (subtitle == null) {
            showToast("Nenhuma legenda neste ponto")
            return
        }
        sentenceStore.prepend(subtitle)
    }

    private fun finishCaptureWithError(userMessage: String, reason: String) {
        finishCapture()
        Log.e(LOG_TAG, "capture failed: $reason")
        showToast(userMessage)
    }

    private fun finishCapture() {
        isCapturing = false
        floatingCaptureButton?.stopPulse()
    }
}
