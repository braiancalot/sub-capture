package com.anonymous.subcapture

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val CaptureBlue = Color(0xFF1565C0)
val StopRed = Color(0xFFB71C1C)
val SelectedRowBackground = Color(0xFF1A2A3A)
val MutedText = Color(0xFF888888)
val FaintText = Color(0xFF555555)

private val subCaptureColors = darkColorScheme(
    primary = CaptureBlue,
    onPrimary = Color.White,
    background = Color(0xFF0D0D0D),
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    surfaceContainer = Color(0xFF1A1A1A),
    outline = Color(0xFF333333),
)

@Composable
fun SubCaptureTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = subCaptureColors, content = content)
}
