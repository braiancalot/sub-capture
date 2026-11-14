package br.com.teshi.subcapture

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

private const val BUBBLE_FADE_MILLIS = 100L
private const val BUBBLE_COLLAPSE_MILLIS = 150L

fun collapseAndRemove(bubbleView: View, parent: ViewGroup) {
    bubbleView.animate().alpha(0f).setDuration(BUBBLE_FADE_MILLIS).withEndAction {
        collapseHeight(bubbleView) { parent.removeView(bubbleView) }
    }
}

private fun collapseHeight(bubbleView: View, onCollapsed: () -> Unit) {
    val layoutParams = bubbleView.layoutParams as LinearLayout.LayoutParams
    val startHeight = bubbleView.height
    val startMargin = layoutParams.bottomMargin
    ValueAnimator.ofFloat(1f, 0f).apply {
        duration = BUBBLE_COLLAPSE_MILLIS
        addUpdateListener { animation ->
            val remaining = animation.animatedValue as Float
            layoutParams.height = (startHeight * remaining).toInt()
            layoutParams.bottomMargin = (startMargin * remaining).toInt()
            bubbleView.layoutParams = layoutParams
        }
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) = onCollapsed()
        })
        start()
    }
}
