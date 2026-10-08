package br.com.teshi.subcapture

import kotlin.math.abs

private const val TAP_SLOP_PIXELS = 10

fun isTapGesture(deltaX: Float, deltaY: Float): Boolean =
    abs(deltaX) < TAP_SLOP_PIXELS && abs(deltaY) < TAP_SLOP_PIXELS

fun snapTargetX(buttonX: Int, buttonWidth: Int, screenWidth: Int, edgeMargin: Int): Int {
    val isOnLeftHalf = buttonX + buttonWidth / 2 < screenWidth / 2
    return if (isOnLeftHalf) edgeMargin else screenWidth - buttonWidth - edgeMargin
}
