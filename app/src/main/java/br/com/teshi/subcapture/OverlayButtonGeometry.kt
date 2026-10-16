package br.com.teshi.subcapture

import kotlin.math.abs

private const val TAP_SLOP_PIXELS = 10

fun isTapGesture(deltaX: Float, deltaY: Float): Boolean =
    abs(deltaX) < TAP_SLOP_PIXELS && abs(deltaY) < TAP_SLOP_PIXELS

fun isWithinCatchRadius(deltaX: Int, deltaY: Int, catchRadius: Int): Boolean =
    deltaX * deltaX + deltaY * deltaY <= catchRadius * catchRadius

fun snapTargetX(buttonX: Int, buttonWidth: Int, screenWidth: Int, edgeMargin: Int): Int =
    edgeXAfterResize(buttonX, buttonWidth, screenWidth, screenWidth, edgeMargin)

fun edgeXAfterResize(buttonX: Int, buttonWidth: Int, oldScreenWidth: Int, newScreenWidth: Int, edgeMargin: Int): Int {
    val isOnLeftHalf = buttonX + buttonWidth / 2 < oldScreenWidth / 2
    return if (isOnLeftHalf) edgeMargin else newScreenWidth - buttonWidth - edgeMargin
}
