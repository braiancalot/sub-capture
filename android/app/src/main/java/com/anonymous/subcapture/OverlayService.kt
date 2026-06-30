package com.anonymous.subcapture

import android.animation.ValueAnimator
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import androidx.core.app.NotificationCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.math.abs

class OverlayService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private val CHANNEL_ID = "overlay_channel"
    private val NOTIFICATION_ID = 1
    private var isCapturing = false

    private var imageReader: ImageReader? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var handlerThread: HandlerThread? = null
    private var captureHandler: Handler? = null

    private var captureButton: Button? = null
    private var pulseAnimator: ValueAnimator? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundWithNotification()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (SubCaptureModule.mediaProjection == null) {
            val resultCode = intent?.getIntExtra("resultCode", Activity.RESULT_CANCELED) ?: Activity.RESULT_CANCELED
            val projectionData: Intent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent?.getParcelableExtra("projectionData", Intent::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent?.getParcelableExtra("projectionData")
            }

            if (resultCode == Activity.RESULT_OK && projectionData != null) {
                val manager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                SubCaptureModule.mediaProjection = manager.getMediaProjection(resultCode, projectionData)
            }
        }

        if (imageReader == null) setupCapture()

        if (!::floatingView.isInitialized) {
            setupOverlayView()
        }

        return START_NOT_STICKY
    }

    private fun setupCapture() {
        val projection = SubCaptureModule.mediaProjection ?: return

        val metrics = resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val density = metrics.densityDpi

        val ht = HandlerThread("ScreenCapture").also { it.start() }
        handlerThread = ht
        captureHandler = Handler(ht.looper)

        val reader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        imageReader = reader

        // Android 14+ exige callback registrado antes de createVirtualDisplay
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            projection.registerCallback(object : MediaProjection.Callback() {
                override fun onStop() {
                    isCapturing = false
                }
            }, Handler(Looper.getMainLooper()))
        }

        virtualDisplay = projection.createVirtualDisplay(
            "SubCapture", width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            reader.surface, null, null
        )
    }

    private fun startForegroundWithNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "SubCapture Overlay", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SubCapture")
            .setContentText("Toque no botão flutuante para capturar legendas")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun setupOverlayView() {
        floatingView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)
        captureButton = floatingView.findViewById(R.id.capture_button)

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val density = resources.displayMetrics.density
        val edgeMargin = (16 * density).toInt()
        val prefs = getSharedPreferences("overlay_prefs", MODE_PRIVATE)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = prefs.getInt("btn_x", edgeMargin)
            y = prefs.getInt("btn_y", (120 * density).toInt())
        }

        windowManager.addView(floatingView, params)

        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        captureButton?.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    v.background?.setHotspot(event.x, event.y)
                    v.isPressed = true
                    v.animate().scaleX(0.85f).scaleY(0.85f).setDuration(100).start()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val screenWidth = resources.displayMetrics.widthPixels
                    val screenHeight = resources.displayMetrics.heightPixels
                    params.x = (initialX + (event.rawX - initialTouchX).toInt())
                        .coerceIn(0, screenWidth - floatingView.width)
                    params.y = (initialY + (event.rawY - initialTouchY).toInt())
                        .coerceIn(0, screenHeight - floatingView.height)
                    windowManager.updateViewLayout(floatingView, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    v.isPressed = false
                    v.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
                    val deltaX = abs(event.rawX - initialTouchX)
                    val deltaY = abs(event.rawY - initialTouchY)
                    if (deltaX < 10 && deltaY < 10) {
                        captureAndOcr()
                    } else {
                        val screenWidth = resources.displayMetrics.widthPixels
                        val targetX = if (params.x + floatingView.width / 2 < screenWidth / 2) {
                            edgeMargin
                        } else {
                            screenWidth - floatingView.width - edgeMargin
                        }
                        prefs.edit().putInt("btn_x", targetX).putInt("btn_y", params.y).apply()
                        animateToEdge(params, targetX)
                    }
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    v.isPressed = false
                    v.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
                    false
                }
                else -> false
            }
        }
    }

    private fun animateToEdge(params: WindowManager.LayoutParams, targetX: Int) {
        ValueAnimator.ofInt(params.x, targetX).apply {
            duration = 280
            interpolator = DecelerateInterpolator(1.8f)
            addUpdateListener { anim ->
                params.x = anim.animatedValue as Int
                try {
                    windowManager.updateViewLayout(floatingView, params)
                } catch (_: Exception) {
                    cancel()
                }
            }
            start()
        }
    }

    private fun startCapturePulse() {
        val btn = captureButton ?: return
        btn.animate().scaleX(0.75f).scaleY(0.75f).setDuration(150).start()
        pulseAnimator = ValueAnimator.ofFloat(1f, 0.35f).apply {
            duration = 550
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            startDelay = 150
            addUpdateListener { btn.alpha = it.animatedValue as Float }
            start()
        }
    }

    private fun stopCapturePulse() {
        pulseAnimator?.cancel()
        pulseAnimator = null
        val btn = captureButton ?: return
        btn.alpha = 1f
        btn.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
    }

    private fun pauseVlc() {
        try {
            sendBroadcast(Intent("org.videolan.vlc.remote.Pause"))
        } catch (_: Exception) {}
    }

    private fun resumeVlc() {
        try {
            sendBroadcast(Intent("org.videolan.vlc.remote.Play"))
        } catch (_: Exception) {}
    }

    private fun captureAndOcr() {
        if (isCapturing) return

        val reader = imageReader
        if (reader == null) {
            SubCaptureModule.sendDebugEvent("Erro: captura não inicializada")
            return
        }

        isCapturing = true
        pauseVlc()
        startCapturePulse()
        SubCaptureModule.sendDebugEvent("Capturando tela...")

        // Drena frames acumulados para desbloquear o surface do VirtualDisplay
        reader.acquireLatestImage()?.close()

        val mainHandler = Handler(Looper.getMainLooper())
        val timeoutRunnable = Runnable {
            reader.setOnImageAvailableListener(null, null)
            isCapturing = false
            resumeVlc()
            stopCapturePulse()
            SubCaptureModule.sendDebugEvent("Erro: app protegido ou sem frame")
        }
        mainHandler.postDelayed(timeoutRunnable, 3000)

        reader.setOnImageAvailableListener({ r ->
            mainHandler.removeCallbacks(timeoutRunnable)
            r.setOnImageAvailableListener(null, null)
            val image = r.acquireLatestImage()
            if (image == null) {
                isCapturing = false
                resumeVlc()
                Handler(Looper.getMainLooper()).post { stopCapturePulse() }
                SubCaptureModule.sendDebugEvent("Erro: frame vazio")
                return@setOnImageAvailableListener
            }

            val bitmap = imageToBitmap(image)
            image.close()

            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                .process(InputImage.fromBitmap(bitmap, 0))
                .addOnSuccessListener { result ->
                    bitmap.recycle()
                    isCapturing = false
                    resumeVlc()
                    Handler(Looper.getMainLooper()).post { stopCapturePulse() }
                    val blocks = result.textBlocks.map { it.text.replace("\n", " ").trim() }
                    val subtitle = if (blocks.isNotEmpty()) blocks.joinToString(" / ") else "Nenhuma legenda encontrada"
                    SubCaptureModule.sendDebugEvent("Concluído", blocks, subtitle)
                    SubCaptureModule.sendSubtitleEvent(subtitle)
                }
                .addOnFailureListener { e ->
                    bitmap.recycle()
                    isCapturing = false
                    resumeVlc()
                    Handler(Looper.getMainLooper()).post { stopCapturePulse() }
                    SubCaptureModule.sendDebugEvent("Erro OCR: ${e.message}")
                }
        }, captureHandler)
    }

    private fun imageToBitmap(image: Image): Bitmap {
        val plane = image.planes[0]
        val buffer = plane.buffer
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride
        val rowPadding = rowStride - pixelStride * image.width

        val raw = Bitmap.createBitmap(
            image.width + rowPadding / pixelStride,
            image.height,
            Bitmap.Config.ARGB_8888
        )
        raw.copyPixelsFromBuffer(buffer)

        return if (rowPadding > 0) {
            val trimmed = Bitmap.createBitmap(raw, 0, 0, image.width, image.height)
            raw.recycle()
            trimmed
        } else {
            raw
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        pulseAnimator?.cancel()
        virtualDisplay?.release()
        imageReader?.close()
        handlerThread?.quit()
        SubCaptureModule.mediaProjection?.stop()
        SubCaptureModule.mediaProjection = null
        SubCaptureModule.projectionGranted = false
        SubCaptureModule.pendingResultCode = Activity.RESULT_CANCELED
        SubCaptureModule.pendingProjectionData = null
        SubCaptureModule.sendDebugEvent("Overlay encerrado")
        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
    }
}
