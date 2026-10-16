package br.com.teshi.subcapture

import android.animation.ValueAnimator
import android.view.View

class CaptureButtonPulse(private val captureButton: View) {
    private var pulseAnimator: ValueAnimator? = null

    fun start() {
        pulseAnimator = ValueAnimator.ofFloat(1f, 0.6f).apply {
            duration = 550
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            // A capture from cues already in memory ends within this delay, so it never blinks.
            startDelay = 300
            addUpdateListener { captureButton.alpha = it.animatedValue as Float }
            start()
        }
    }

    fun stop() {
        pulseAnimator?.cancel()
        pulseAnimator = null
        captureButton.animate().alpha(1f).setDuration(150).start()
    }
}
