package br.com.teshi.subcapture

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith

private const val QUICK_FADE_MILLIS = 150
private const val FAB_HIDDEN_SCALE = 0.6f

fun quickCrossfade(): ContentTransform = fadeIn(tween(QUICK_FADE_MILLIS)) togetherWith fadeOut(tween(QUICK_FADE_MILLIS))

fun fabSwap(): ContentTransform {
    val enter = scaleIn(tween(QUICK_FADE_MILLIS * 2), FAB_HIDDEN_SCALE) + fadeIn(tween(QUICK_FADE_MILLIS))
    val exit = scaleOut(tween(QUICK_FADE_MILLIS), FAB_HIDDEN_SCALE) + fadeOut(tween(QUICK_FADE_MILLIS))
    // Unclipped, or the button's shadow is cut square while it grows
    return ContentTransform(enter, exit, sizeTransform = SizeTransform(clip = false))
}
