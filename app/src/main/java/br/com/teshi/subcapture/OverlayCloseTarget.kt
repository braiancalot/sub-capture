package br.com.teshi.subcapture

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ImageView

private const val TARGET_SIZE_DP = 56
private const val TARGET_BOTTOM_MARGIN_DP = 56
private const val CATCH_RADIUS_DP = 64

class OverlayCloseTarget(private val context: Context) {
    private val windowManager = context.getSystemService(WindowManager::class.java)
    private val density = context.resources.displayMetrics.density
    private val targetBackground = GradientDrawable().apply { shape = GradientDrawable.OVAL }
    private val targetView = buildTargetView()

    // MUST be attached before the dragged button: windows stack in the order they are added
    fun attach() {
        targetView.visibility = View.INVISIBLE
        windowManager.addView(targetView, targetLayoutParams())
    }

    fun detach() {
        windowManager.removeView(targetView)
    }

    fun show() {
        if (targetView.visibility == View.VISIBLE) return
        showCatching(false)
        targetView.visibility = View.VISIBLE
    }

    fun hide() {
        targetView.visibility = View.INVISIBLE
    }

    fun isCatching(draggedView: View): Boolean {
        if (targetView.visibility != View.VISIBLE || !targetView.isLaidOut) return false
        val (draggedX, draggedY) = centerOnScreen(draggedView)
        val (targetX, targetY) = centerOnScreen(targetView)
        return isWithinCatchRadius(draggedX - targetX, draggedY - targetY, (CATCH_RADIUS_DP * density).toInt())
    }

    fun showCatching(isCatching: Boolean) {
        val fillColor = if (isCatching) R.color.closeTargetCatching else R.color.closeTargetIdle
        targetBackground.setColor(context.getColor(fillColor))
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
        val targetSize = (TARGET_SIZE_DP * density).toInt()
        return WindowManager.LayoutParams(
            targetSize,
            targetSize,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = (TARGET_BOTTOM_MARGIN_DP * density).toInt()
        }
    }
}
