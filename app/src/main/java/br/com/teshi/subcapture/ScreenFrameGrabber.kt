package br.com.teshi.subcapture

import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.DisplayMetrics
import java.util.concurrent.atomic.AtomicBoolean

private const val FRAME_TIMEOUT_MILLIS = 3000L

class ScreenFrameGrabber(
    private val mediaProjection: MediaProjection,
    displayMetrics: DisplayMetrics,
    onProjectionStopped: () -> Unit,
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val captureThread = HandlerThread("ScreenCapture").apply { start() }
    private val captureHandler = Handler(captureThread.looper)
    private val densityDpi = displayMetrics.densityDpi
    private var imageReader = newImageReader(displayMetrics.widthPixels, displayMetrics.heightPixels)
    private val virtualDisplay: VirtualDisplay?

    init {
        // Android 14+ REQUIRES the callback to be registered before createVirtualDisplay
        mediaProjection.registerCallback(object : MediaProjection.Callback() {
            override fun onStop(): Unit = onProjectionStopped()

            override fun onCapturedContentResize(width: Int, height: Int): Unit = resizeCapture(width, height)
        }, mainHandler)
        virtualDisplay = mediaProjection.createVirtualDisplay(
            "SubCapture", imageReader.width, imageReader.height, densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, imageReader.surface, null, null
        )
    }

    private fun resizeCapture(width: Int, height: Int) {
        if (width == imageReader.width && height == imageReader.height) return
        val staleReader = imageReader
        imageReader = newImageReader(width, height)
        virtualDisplay?.resize(width, height, densityDpi)
        virtualDisplay?.surface = imageReader.surface
        staleReader.setOnImageAvailableListener(null, null)
        // Closing on the capture thread keeps it from racing a frame still being copied there
        captureHandler.post { staleReader.close() }
    }

    fun grabFrame(onFrame: (Bitmap) -> Unit, onFailure: (String) -> Unit) {
        val isSettled = AtomicBoolean(false)
        val frameTimeout = Runnable {
            if (!isSettled.compareAndSet(false, true)) return@Runnable
            imageReader.setOnImageAvailableListener(null, null)
            onFailure("no frame after ${FRAME_TIMEOUT_MILLIS}ms (FLAG_SECURE app or nothing redrawn)")
        }
        // Drains queued frames to unblock the VirtualDisplay surface
        imageReader.acquireLatestImage()?.close()
        mainHandler.postDelayed(frameTimeout, FRAME_TIMEOUT_MILLIS)
        imageReader.setOnImageAvailableListener({ reader ->
            if (!isSettled.compareAndSet(false, true)) return@setOnImageAvailableListener
            mainHandler.removeCallbacks(frameTimeout)
            deliverLatestFrame(reader, onFrame, onFailure)
        }, captureHandler)
    }

    private fun deliverLatestFrame(reader: ImageReader, onFrame: (Bitmap) -> Unit, onFailure: (String) -> Unit) {
        reader.setOnImageAvailableListener(null, null)
        val frame = reader.acquireLatestImage()?.use(::imageToBitmap)
        mainHandler.post {
            if (frame == null) {
                onFailure("ImageReader signalled a frame but acquireLatestImage returned null")
            } else {
                onFrame(frame)
            }
        }
    }

    fun release() {
        imageReader.setOnImageAvailableListener(null, null)
        mainHandler.removeCallbacksAndMessages(null)
        virtualDisplay?.release()
        imageReader.close()
        captureThread.quit()
        mediaProjection.stop()
    }
}

private fun newImageReader(width: Int, height: Int): ImageReader =
    ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)

private fun imageToBitmap(image: Image): Bitmap {
    val plane = image.planes[0]
    val rowPadding = plane.rowStride - plane.pixelStride * image.width
    val paddedBitmap = Bitmap.createBitmap(
        image.width + rowPadding / plane.pixelStride, image.height, Bitmap.Config.ARGB_8888
    )
    paddedBitmap.copyPixelsFromBuffer(plane.buffer)
    if (rowPadding == 0) return paddedBitmap

    val trimmedBitmap = Bitmap.createBitmap(paddedBitmap, 0, 0, image.width, image.height)
    paddedBitmap.recycle()
    return trimmedBitmap
}
