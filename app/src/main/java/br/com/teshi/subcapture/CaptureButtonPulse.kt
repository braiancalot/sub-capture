package br.com.teshi.subcapture

import android.animation.ValueAnimator
import android.view.View

class CaptureButtonPulse(private val captureButton: View) {
    private var pulseAnimator: ValueAnimator? = null

    fun start() {
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

    fun stop() {
        pulseAnimator?.cancel()
        pulseAnimator = null
        captureButton.alpha = 1f
        captureButton.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
    }
}
