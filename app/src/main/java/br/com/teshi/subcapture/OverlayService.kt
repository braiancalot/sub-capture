package br.com.teshi.subcapture

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val LOG_TAG = "OverlayService"
private const val NOTIFICATION_CHANNEL_ID = "overlay_channel"
private const val NOTIFICATION_ID = 1
private const val RESULT_CODE_EXTRA = "resultCode"
private const val CONSENT_INTENT_EXTRA = "consentIntent"
private const val VLC_PAUSE_ACTION = "org.videolan.vlc.remote.Pause"
private const val VLC_PLAY_ACTION = "org.videolan.vlc.remote.Play"

class OverlayService : Service() {
    companion object {
        private val runningState = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = runningState.asStateFlow()

        fun start(context: Context, resultCode: Int, consentIntent: Intent) {
            val startIntent = Intent(context, OverlayService::class.java)
                .putExtra(RESULT_CODE_EXTRA, resultCode)
                .putExtra(CONSENT_INTENT_EXTRA, consentIntent)
            context.startForegroundService(startIntent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, OverlayService::class.java))
        }
    }

    private val subtitleRecognizer = SubtitleRecognizer()
    private var screenFrameGrabber: ScreenFrameGrabber? = null
    private var floatingCaptureButton: FloatingCaptureButton? = null
    private var isCapturing = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        // getMediaProjection MUST come after startForeground with the mediaProjection type
        startForeground(
            NOTIFICATION_ID, buildNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
        )
        runningState.value = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (screenFrameGrabber != null) return START_NOT_STICKY
        val mediaProjection = mediaProjectionFrom(intent)
        if (mediaProjection == null) {
            Log.e(LOG_TAG, "start intent carried no usable MediaProjection consent, extras=${intent?.extras}")
            stopSelf()
            return START_NOT_STICKY
        }
        screenFrameGrabber = ScreenFrameGrabber(mediaProjection, resources.displayMetrics, ::stopSelf)
        floatingCaptureButton = FloatingCaptureButton(this, ::captureSubtitle).also { it.show() }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        floatingCaptureButton?.remove()
        screenFrameGrabber?.release()
        subtitleRecognizer.close()
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
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .build()
    }

    private fun mediaProjectionFrom(intent: Intent?): MediaProjection? {
        val resultCode = intent?.getIntExtra(RESULT_CODE_EXTRA, Activity.RESULT_CANCELED)
        val consentIntent = intent?.getParcelableExtra(CONSENT_INTENT_EXTRA, Intent::class.java)
        if (resultCode != Activity.RESULT_OK || consentIntent == null) return null
        return getSystemService(MediaProjectionManager::class.java).getMediaProjection(resultCode, consentIntent)
    }

    private fun captureSubtitle() {
        val frameGrabber = screenFrameGrabber ?: return
        if (isCapturing) return
        isCapturing = true
        sendBroadcast(Intent(VLC_PAUSE_ACTION))
        floatingCaptureButton?.startPulse()
        frameGrabber.grabFrame(::recognizeSubtitle) { reason ->
            finishCaptureWithError("Sem imagem da tela (app protegido?)", reason)
        }
    }

    private fun recognizeSubtitle(frame: Bitmap) {
        subtitleRecognizer.recognize(frame, ::finishCaptureWithSubtitle) { reason ->
            finishCaptureWithError("Falha no reconhecimento de texto", reason)
        }
    }

    private fun finishCaptureWithSubtitle(subtitle: String?) {
        finishCapture()
        if (subtitle == null) {
            showToast("Nenhuma legenda encontrada")
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
        sendBroadcast(Intent(VLC_PLAY_ACTION))
        floatingCaptureButton?.stopPulse()
    }
}
