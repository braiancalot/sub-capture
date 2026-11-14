package br.com.teshi.subcapture

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView

private const val TARGET_SIZE_DP = 56
private const val TARGET_BOTTOM_MARGIN_DP = 56
private const val CATCH_RADIUS_DP = 64
private const val TARGET_RISE_DP = 24
private const val TARGET_CATCHING_SCALE = 1.2f
private const val TARGET_SHOW_MILLIS = 200L
private const val TARGET_HIDE_MILLIS = 150L
private const val TARGET_SLACK_DP = 8

class OverlayCloseTarget(private val context: Context) {
    private val windowManager = context.getSystemService(WindowManager::class.java)
    private val density = context.resources.displayMetrics.density
    private val targetBackground = GradientDrawable().apply { shape = GradientDrawable.OVAL }
    private val targetView = buildTargetView()
    private val slack = (TARGET_SLACK_DP * density).toInt()
    private val rise = (TARGET_RISE_DP * density).toInt()
    // Room around the circle, or the window cuts it while it rises and grows
    private val targetFrame = FrameLayout(context).apply {
        setPadding(slack, slack, slack, slack + rise)
        clipToPadding = false
        val targetSize = (TARGET_SIZE_DP * density).toInt()
        addView(targetView, FrameLayout.LayoutParams(targetSize, targetSize))
    }
    private var isTargetShown = false
    private var isCatchingShown = false

    // MUST be attached before the dragged button: windows stack in the order they are added
    fun attach() {
        targetView.visibility = View.INVISIBLE
        windowManager.addView(targetFrame, targetLayoutParams())
    }

    fun detach() {
        windowManager.removeView(targetFrame)
    }

    fun show() {
        if (isTargetShown) return
        isTargetShown = true
        isCatchingShown = false
        targetBackground.setColor(context.getColor(R.color.raisedSurface))
        targetView.apply {
            scaleX = 1f
            scaleY = 1f
            alpha = 0f
            translationY = rise.toFloat()
            visibility = View.VISIBLE
        }
        targetView.animate().alpha(1f).translationY(0f).setDuration(TARGET_SHOW_MILLIS).start()
    }

    fun hide() {
        if (!isTargetShown) return
        isTargetShown = false
        targetView.animate().alpha(0f).translationY(rise.toFloat()).setDuration(TARGET_HIDE_MILLIS)
            .withEndAction { if (!isTargetShown) targetView.visibility = View.INVISIBLE }.start()
    }

    fun isCatching(draggedView: View): Boolean {
        if (!isTargetShown || !targetView.isLaidOut) return false
        val (draggedX, draggedY) = centerOnScreen(draggedView)
        val (targetX, targetY) = centerOnScreen(targetView)
        return isWithinCatchRadius(draggedX - targetX, draggedY - targetY, (CATCH_RADIUS_DP * density).toInt())
    }

    fun showCatching(isCatching: Boolean) {
        if (isCatching == isCatchingShown) return
        isCatchingShown = isCatching
        val fillColor = if (isCatching) R.color.dangerCoral else R.color.raisedSurface
        targetBackground.setColor(context.getColor(fillColor))
        val scale = if (isCatching) TARGET_CATCHING_SCALE else 1f
        targetView.animate().scaleX(scale).scaleY(scale).setDuration(TARGET_HIDE_MILLIS).start()
        val feedback =
            if (isCatching) HapticFeedbackConstants.GESTURE_THRESHOLD_ACTIVATE
            else HapticFeedbackConstants.GESTURE_THRESHOLD_DEACTIVATE
        targetView.performHapticFeedback(feedback)
    }

    private fun centerOnScreen(view: View): Pair<Int, Int> {
        val topLeft = IntArray(2)
        view.getLocationOnScreen(topLeft)
        return Pair(topLeft[0] + view.width / 2, topLeft[1] + view.height / 2)
    }

    private fun buildTargetView(): ImageView = ImageView(context).apply {
        val iconInset = (16 * density).toInt()
        setPadding(iconInset, iconInset, iconInset, iconInset)
        setImageResource(R.drawable.icon_close)
        background = targetBackground
    }

    private fun targetLayoutParams(): WindowManager.LayoutParams {
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = (TARGET_BOTTOM_MARGIN_DP * density).toInt() - slack - rise
        }
    }
}
