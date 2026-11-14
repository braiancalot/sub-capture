package br.com.teshi.subcapture

import android.animation.LayoutTransition
import android.content.Context
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout

private const val BUBBLE_VISIBLE_MILLIS = 5000L
private const val BUBBLE_GAP_DP = 8
private const val BUBBLE_SHIFT_MILLIS = 150L

class CaptureFeedbackBubble(private val context: Context, private val onDiscard: (CapturedSentence) -> Unit) {
    private val windowManager = context.getSystemService(WindowManager::class.java)
    private val hideHandler = Handler(Looper.getMainLooper())
    private val density = context.resources.displayMetrics.density
    private val bubbleGap = (BUBBLE_GAP_DP * density).toInt()
    private val stackView = buildStackView()
    private val bubbleViews = mutableMapOf<CaptureBubble, View>()
    private var stack = emptyList<CaptureBubble>()
    private var isShown = false
    private var isAttached = false

    fun show(message: String, addedSentence: CapturedSentence?) =
        push(CaptureBubble(message, isFailure = false, addedSentence))

    fun showFailure(message: String) = push(CaptureBubble(message, isFailure = true, addedSentence = null))

    fun remove() {
        hideHandler.removeCallbacksAndMessages(null)
        stack = emptyList()
        stackView.animate().cancel()
        detachWindow()
    }

    private fun push(bubble: CaptureBubble) {
        redraw(pushedCaptureBubble(stack, bubble))
        hideHandler.removeCallbacksAndMessages(null)
        hideHandler.postDelayed({ redraw(emptyList()) }, BUBBLE_VISIBLE_MILLIS)
    }

    private fun discard(bubble: CaptureBubble) {
        bubble.addedSentence?.let(onDiscard)
        redraw(withoutCaptureBubble(stack, bubble))
    }

    private fun redraw(updatedStack: List<CaptureBubble>) {
        val leavingBubbles = stack.filterNot { it in updatedStack }
        stack = updatedStack
        if (stack.isEmpty()) return fadeWindowOut()
        if (!isShown) restartWindow()
        leavingBubbles.forEach { bubble -> bubbleViews.remove(bubble)?.let { collapseAndRemove(it, stackView) } }
        placeOnTop(stack.first())
        if (!isAttached) windowManager.addView(stackView, stackLayoutParams())
        isAttached = true
        isShown = true
    }

    private fun restartWindow() {
        stackView.animate().cancel()
        detachWindow()
    }

    // A push always lands on top, and a discard only removes, so the top is the only place a view moves to
    private fun placeOnTop(bubble: CaptureBubble) {
        val bubbleView = bubbleViews.getOrPut(bubble) { stackedBubbleView(bubble) }
        val currentTop = (0 until stackView.childCount).map(stackView::getChildAt).firstOrNull { it in bubbleViews.values }
        if (currentTop === bubbleView) return
        stackView.removeView(bubbleView)
        stackView.addView(bubbleView, 0)
    }

    private fun stackedBubbleView(bubble: CaptureBubble): View =
        captureBubbleView(context, bubble) { discard(bubble) }.apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply { bottomMargin = bubbleGap; gravity = Gravity.CENTER_HORIZONTAL }
        }

    private fun fadeWindowOut() {
        if (!isShown) return
        isShown = false
        stackView.animate().alpha(0f).setDuration(BUBBLE_SHIFT_MILLIS).withEndAction(::detachWindow).start()
    }

    private fun detachWindow() {
        isShown = false
        if (!isAttached) return
        windowManager.removeView(stackView)
        isAttached = false
        stackView.alpha = 1f
        bubbleViews.clear()
        stackView.removeAllViews()
    }

    private fun buildStackView(): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        // A leaving bubble fades and collapses itself: the stock removal would be cut by the window shrinking first
        layoutTransition = LayoutTransition().apply {
            disableTransitionType(LayoutTransition.DISAPPEARING)
            disableTransitionType(LayoutTransition.CHANGE_DISAPPEARING)
            setDuration(BUBBLE_SHIFT_MILLIS)
            // The stock delay holds a new bubble back until the others have moved, so a capture showed late
            setStartDelay(LayoutTransition.APPEARING, 0)
        }
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
