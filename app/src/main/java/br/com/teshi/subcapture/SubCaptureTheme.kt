package br.com.teshi.subcapture

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.dp

val AppBackground = Color(0xFF0A0A0B)
val CardSurface = Color(0xFF121113)
val RaisedSurface = Color(0xFF1A181B)
val PrimaryText = Color(0xFFF4F1EC)
val SecondaryText = Color(0xFFB4AFA7)
val MutedText = Color(0xFF7C776F)
val FaintText = Color(0xFF514D47)
val AccentLavender = Color(0xFFC4B5FD)
val OnAccentInk = Color(0xFF1C1633)
val AccentTint = AccentLavender.copy(alpha = 0.12f)
val AccentLine = AccentLavender.copy(alpha = 0.40f)
val SelectedCardSurface = AccentTint.compositeOver(AppBackground)
val DangerCoral = Color(0xFFE8806B)
val DangerTint = DangerCoral.copy(alpha = 0.12f).compositeOver(AppBackground)

val ScreenGutter = 20.dp
val CardGap = 12.dp
val CardRadius = 14.dp

private val subCaptureColors = darkColorScheme(
    primary = AccentLavender,
    onPrimary = OnAccentInk,
    background = AppBackground,
    onBackground = PrimaryText,
    surface = CardSurface,
    onSurface = PrimaryText,
    surfaceContainer = RaisedSurface,
    outline = FaintText,
)

@Composable
fun SubCaptureTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = subCaptureColors, content = content)
}
