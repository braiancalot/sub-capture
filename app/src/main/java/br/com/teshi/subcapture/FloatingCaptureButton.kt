package br.com.teshi.subcapture

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.ComponentCallbacks
import android.content.Context
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.Button

private const val POSITION_PREFS_NAME = "overlay_button_position"
private const val POSITION_X_KEY = "button_x"
private const val POSITION_Y_KEY = "button_y"

@SuppressLint("InflateParams", "ClickableViewAccessibility")
class FloatingCaptureButton(
    private val context: Context,
    private val onTap: () -> Unit,
    private val onLongPress: () -> Unit,
    private val onClose: () -> Unit,
) {
    private val windowManager = context.getSystemService(WindowManager::class.java)
    private val positionPrefs = context.getSharedPreferences(POSITION_PREFS_NAME, Context.MODE_PRIVATE)
    private val density = context.resources.displayMetrics.density
    private val edgeMargin = (16 * density).toInt()
    private val overlayView: View = LayoutInflater.from(context).inflate(R.layout.overlay_layout, null)
    private val captureButton: Button = overlayView.findViewById(R.id.capture_button)
    private val layoutParams = savedPositionLayoutParams()
    private val closeTarget = OverlayCloseTarget(context)
    val pulse = CaptureButtonPulse(captureButton)
    private val longPressHandler = Handler(Looper.getMainLooper())
    private var wasLongPressed = false

    private var dragStartX = 0
    private var dragStartY = 0
    private var touchStartX = 0f
    private var touchStartY = 0f
    private var lastScreenWidth = context.resources.displayMetrics.widthPixels
    private val screenResizeCallbacks = object : ComponentCallbacks {
        override fun onConfigurationChanged(newConfig: Configuration) = moveInsideResizedScreen()

        @Deprecated("Deprecated in Java")
        override fun onLowMemory() = Unit
    }

    fun show() {
        closeTarget.attach()
        windowManager.addView(overlayView, layoutParams)
        captureButton.setOnTouchListener { _, event -> handleTouch(event) }
        context.registerComponentCallbacks(screenResizeCallbacks)
    }

    fun remove() {
        context.unregisterComponentCallbacks(screenResizeCallbacks)
        longPressHandler.removeCallbacksAndMessages(null)
        pulse.stop()
        closeTarget.detach()
        windowManager.removeView(overlayView)
    }

    private fun moveInsideResizedScreen() {
        val screen = context.resources.displayMetrics
        if (screen.widthPixels == lastScreenWidth) return
        layoutParams.x = edgeXAfterResize(layoutParams.x, overlayView.width, lastScreenWidth, screen.widthPixels, edgeMargin)
        layoutParams.y = layoutParams.y.coerceIn(0, screen.heightPixels - overlayView.height)
        lastScreenWidth = screen.widthPixels
        windowManager.updateViewLayout(overlayView, layoutParams)
        savePosition(layoutParams.x)
    }

    private fun savePosition(x: Int) {
        positionPrefs.edit().putInt(POSITION_X_KEY, x).putInt(POSITION_Y_KEY, layoutParams.y).apply()
    }

    private fun savedPositionLayoutParams(): WindowManager.LayoutParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.TOP or Gravity.START
        x = positionPrefs.getInt(POSITION_X_KEY, edgeMargin)
        y = positionPrefs.getInt(POSITION_Y_KEY, (120 * density).toInt())
    }

    private fun handleTouch(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> beginTouch(event)
            MotionEvent.ACTION_MOVE -> dragTo(event)
            MotionEvent.ACTION_UP -> endTouch(event)
            MotionEvent.ACTION_CANCEL -> cancelTouch()
            else -> return false
        }
        return true
    }

    private fun beginTouch(event: MotionEvent) {
        dragStartX = layoutParams.x
        dragStartY = layoutParams.y
        touchStartX = event.rawX
        touchStartY = event.rawY
        captureButton.background?.setHotspot(event.x, event.y)
        wasLongPressed = false
        // Android's own delay before a press shows, so a drag that starts at once never flashes it
        longPressHandler.postDelayed(::showPressedLook, ViewConfiguration.getTapTimeout().toLong())
        longPressHandler.postDelayed(::longPress, ViewConfiguration.getLongPressTimeout().toLong())
    }

    private fun showPressedLook() {
        captureButton.isPressed = true
        captureButton.animate().scaleX(0.85f).scaleY(0.85f).setDuration(100).start()
    }

    private fun longPress() {
        wasLongPressed = true
        releasePressedLook()
        captureButton.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        onLongPress()
    }

    private fun dragTo(event: MotionEvent) {
        if (wasLongPressed) return
        val screen = context.resources.displayMetrics
        layoutParams.x = (dragStartX + (event.rawX - touchStartX).toInt())
            .coerceIn(0, screen.widthPixels - overlayView.width)
        layoutParams.y = (dragStartY + (event.rawY - touchStartY).toInt())
            .coerceIn(0, screen.heightPixels - overlayView.height)
        windowManager.updateViewLayout(overlayView, layoutParams)
        if (!isTapGesture(event.rawX - touchStartX, event.rawY - touchStartY)) beginDrag()
        closeTarget.showCatching(closeTarget.isCatching(overlayView))
    }

    private fun beginDrag() {
        longPressHandler.removeCallbacksAndMessages(null)
        if (captureButton.isPressed) releasePressedLook()
        closeTarget.show()
    }

    private fun endTouch(event: MotionEvent) {
        longPressHandler.removeCallbacksAndMessages(null)
        if (wasLongPressed) return
        val isTap = isTapGesture(event.rawX - touchStartX, event.rawY - touchStartY)
        if (isTap && !captureButton.isPressed) showPressedLook()
        releasePressedLook()
        val wasDroppedOnCloseTarget = closeTarget.isCatching(overlayView)
        closeTarget.hide()
        if (isTap) {
            onTap()
            return
        }
        if (wasDroppedOnCloseTarget) {
            onClose()
            return
        }
        val screenWidth = context.resources.displayMetrics.widthPixels
        val targetX = snapTargetX(layoutParams.x, overlayView.width, screenWidth, edgeMargin)
        savePosition(targetX)
        animateToEdge(targetX)
    }

    private fun cancelTouch() {
        longPressHandler.removeCallbacksAndMessages(null)
        releasePressedLook()
        closeTarget.hide()
    }

    private fun releasePressedLook() {
        captureButton.isPressed = false
        captureButton.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
    }

    private fun animateToEdge(targetX: Int) {
        ValueAnimator.ofInt(layoutParams.x, targetX).apply {
            duration = 280
            interpolator = DecelerateInterpolator(1.8f)
            addUpdateListener { animation ->
                layoutParams.x = animation.animatedValue as Int
                if (overlayView.isAttachedToWindow) windowManager.updateViewLayout(overlayView, layoutParams)
            }
            start()
        }
    }
}
