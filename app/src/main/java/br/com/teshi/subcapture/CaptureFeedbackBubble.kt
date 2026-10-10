package br.com.teshi.subcapture

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView

private const val BUBBLE_VISIBLE_MILLIS = 3500L
private const val BUBBLE_MAX_WIDTH_FRACTION = 0.8f

class CaptureFeedbackBubble(private val context: Context) {
    private val windowManager = context.getSystemService(WindowManager::class.java)
    private val hideHandler = Handler(Looper.getMainLooper())
    private val density = context.resources.displayMetrics.density
    private val bubbleView = buildBubbleView()
    private var isShown = false

    fun show(message: String) {
        bubbleView.text = message
        bubbleView.maxWidth = (context.resources.displayMetrics.widthPixels * BUBBLE_MAX_WIDTH_FRACTION).toInt()
        if (!isShown) windowManager.addView(bubbleView, bubbleLayoutParams())
        isShown = true
        hideHandler.removeCallbacksAndMessages(null)
        hideHandler.postDelayed(::hide, BUBBLE_VISIBLE_MILLIS)
    }

    fun remove() {
        hideHandler.removeCallbacksAndMessages(null)
        hide()
    }

    private fun hide() {
        if (!isShown) return
        windowManager.removeView(bubbleView)
        isShown = false
    }

    private fun buildBubbleView(): TextView = TextView(context).apply {
        val padding = (12 * density).toInt()
        setPadding(padding, padding, padding, padding)
        setTextColor(Color.WHITE)
        textSize = 15f
        maxLines = 4
        ellipsize = TextUtils.TruncateAt.END
        gravity = Gravity.CENTER
        background = GradientDrawable().apply {
            setColor(context.getColor(R.color.iconBackground))
            cornerRadius = 12 * density
        }
    }

    private fun bubbleLayoutParams(): WindowManager.LayoutParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        y = (48 * density).toInt()
        // Android 12+ blocks touches passing through an overlay more opaque than this
        alpha = 0.8f
    }
}
