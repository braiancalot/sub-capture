package br.com.teshi.subcapture

import android.content.Context
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.widget.LinearLayout

private const val BUBBLE_VISIBLE_MILLIS = 5000L
private const val BUBBLE_GAP_DP = 8

class CaptureFeedbackBubble(private val context: Context, private val onDiscard: (CapturedSentence) -> Unit) {
    private val windowManager = context.getSystemService(WindowManager::class.java)
    private val hideHandler = Handler(Looper.getMainLooper())
    private val density = context.resources.displayMetrics.density
    private val stackView = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
    private var stack = emptyList<CaptureBubble>()
    private var isShown = false

    fun show(message: String, addedSentence: CapturedSentence?) =
        push(CaptureBubble(message, isFailure = false, addedSentence))

    fun showFailure(message: String) = push(CaptureBubble(message, isFailure = true, addedSentence = null))

    fun remove() {
        hideHandler.removeCallbacksAndMessages(null)
        redraw(emptyList())
    }

    private fun push(bubble: CaptureBubble) {
        redraw(pushedCaptureBubble(stack, bubble))
        hideHandler.removeCallbacksAndMessages(null)
        hideHandler.postDelayed(::remove, BUBBLE_VISIBLE_MILLIS)
    }

    private fun discard(bubble: CaptureBubble) {
        bubble.addedSentence?.let(onDiscard)
        redraw(withoutCaptureBubble(stack, bubble))
    }

    private fun redraw(updatedStack: List<CaptureBubble>) {
        stack = updatedStack
        stackView.removeAllViews()
        stack.forEachIndexed { position, bubble -> stackView.addView(stackedBubbleView(bubble, position)) }
        if (stack.isEmpty()) return hideWindow()
        if (!isShown) windowManager.addView(stackView, stackLayoutParams())
        isShown = true
    }

    private fun stackedBubbleView(bubble: CaptureBubble, position: Int): LinearLayout =
        captureBubbleView(context, bubble) { discard(bubble) }.apply {
            val gap = if (position == 0) 0 else (BUBBLE_GAP_DP * density).toInt()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply { topMargin = gap; gravity = Gravity.CENTER_HORIZONTAL }
        }

    private fun hideWindow() {
        if (!isShown) return
        windowManager.removeView(stackView)
        isShown = false
    }

    private fun stackLayoutParams(): WindowManager.LayoutParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        y = (48 * density).toInt()
        alpha = 0.8f
    }
}
