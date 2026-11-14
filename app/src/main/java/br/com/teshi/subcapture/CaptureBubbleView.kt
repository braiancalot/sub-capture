package br.com.teshi.subcapture

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

private const val BUBBLE_MAX_WIDTH_FRACTION = 0.8f
private const val DISCARD_TOUCH_SIZE_DP = 40
private const val DISCARD_ICON_PADDING_DP = 10

fun captureBubbleView(context: Context, bubble: CaptureBubble, onDiscard: () -> Unit): LinearLayout =
    LinearLayout(context).apply {
        val density = context.resources.displayMetrics.density
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding((12 * density).toInt(), (4 * density).toInt(), (4 * density).toInt(), (4 * density).toInt())
        background = GradientDrawable().apply {
            setColor(context.getColor(R.color.raisedSurface))
            cornerRadius = 14 * density
        }
        addView(bubbleMessageView(context, bubble))
        addView(discardButtonView(context, onDiscard))
    }

private fun bubbleMessageView(context: Context, bubble: CaptureBubble): TextView = TextView(context).apply {
    val displayMetrics = context.resources.displayMetrics
    val verticalPadding = (8 * displayMetrics.density).toInt()
    setPadding(0, verticalPadding, 0, verticalPadding)
    text = bubble.message
    setTextColor(context.getColor(if (bubble.isFailure) R.color.dangerCoral else R.color.primaryText))
    textSize = 15f
    typeface = context.resources.getFont(R.font.figtree_regular)
    maxLines = 4
    ellipsize = TextUtils.TruncateAt.END
    gravity = Gravity.CENTER
    val discardWidth = DISCARD_TOUCH_SIZE_DP * displayMetrics.density
    maxWidth = (displayMetrics.widthPixels * BUBBLE_MAX_WIDTH_FRACTION - discardWidth).toInt()
}

private fun discardButtonView(context: Context, onDiscard: () -> Unit): ImageView = ImageView(context).apply {
    val density = context.resources.displayMetrics.density
    val touchSize = (DISCARD_TOUCH_SIZE_DP * density).toInt()
    layoutParams = LinearLayout.LayoutParams(touchSize, touchSize)
    val iconPadding = (DISCARD_ICON_PADDING_DP * density).toInt()
    setPadding(iconPadding, iconPadding, iconPadding, iconPadding)
    setImageResource(R.drawable.icon_close)
    imageTintList = ColorStateList.valueOf(context.getColor(R.color.secondaryText))
    contentDescription = context.getString(R.string.capture_discard)
    setOnClickListener { onDiscard() }
}
