package br.com.teshi.subcapture

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.Button

private const val POSITION_PREFS_NAME = "overlay_prefs"
private const val POSITION_X_KEY = "btn_x"
private const val POSITION_Y_KEY = "btn_y"

@SuppressLint("InflateParams", "ClickableViewAccessibility")
class FloatingCaptureButton(private val context: Context, private val onTap: () -> Unit) {
    private val windowManager = context.getSystemService(WindowManager::class.java)
    private val positionPrefs = context.getSharedPreferences(POSITION_PREFS_NAME, Context.MODE_PRIVATE)
    private val density = context.resources.displayMetrics.density
    private val edgeMargin = (16 * density).toInt()
    private val overlayView: View = LayoutInflater.from(context).inflate(R.layout.overlay_layout, null)
    private val captureButton: Button = overlayView.findViewById(R.id.capture_button)
    private val layoutParams = savedPositionLayoutParams()

    private var pulseAnimator: ValueAnimator? = null
    private var dragStartX = 0
    private var dragStartY = 0
    private var touchStartX = 0f
    private var touchStartY = 0f

    fun show() {
        windowManager.addView(overlayView, layoutParams)
        captureButton.setOnTouchListener { _, event -> handleTouch(event) }
    }

    fun remove() {
        pulseAnimator?.cancel()
        windowManager.removeView(overlayView)
    }

    fun startPulse() {
        captureButton.animate().scaleX(0.75f).scaleY(0.75f).setDuration(150).start()
        pulseAnimator = ValueAnimator.ofFloat(1f, 0.35f).apply {
            duration = 550
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            startDelay = 150
            addUpdateListener { captureButton.alpha = it.animatedValue as Float }
            start()
        }
    }

    fun stopPulse() {
        pulseAnimator?.cancel()
        pulseAnimator = null
        captureButton.alpha = 1f
        captureButton.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
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
            MotionEvent.ACTION_CANCEL -> releasePressedLook()
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
        captureButton.isPressed = true
        captureButton.animate().scaleX(0.85f).scaleY(0.85f).setDuration(100).start()
    }

    private fun dragTo(event: MotionEvent) {
        val screen = context.resources.displayMetrics
        layoutParams.x = (dragStartX + (event.rawX - touchStartX).toInt())
            .coerceIn(0, screen.widthPixels - overlayView.width)
        layoutParams.y = (dragStartY + (event.rawY - touchStartY).toInt())
            .coerceIn(0, screen.heightPixels - overlayView.height)
        windowManager.updateViewLayout(overlayView, layoutParams)
    }

    private fun endTouch(event: MotionEvent) {
        releasePressedLook()
        if (isTapGesture(event.rawX - touchStartX, event.rawY - touchStartY)) {
            onTap()
            return
        }
        val screenWidth = context.resources.displayMetrics.widthPixels
        val targetX = snapTargetX(layoutParams.x, overlayView.width, screenWidth, edgeMargin)
        positionPrefs.edit().putInt(POSITION_X_KEY, targetX).putInt(POSITION_Y_KEY, layoutParams.y).apply()
        animateToEdge(targetX)
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
