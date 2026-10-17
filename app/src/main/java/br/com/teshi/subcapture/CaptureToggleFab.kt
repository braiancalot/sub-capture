package br.com.teshi.subcapture

import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

private val startCaptureColors = ScreenFabColors(AccentLavender, OnAccentInk)
private val stopCaptureColors = ScreenFabColors(DangerTint, DangerCoral)

// Icon only: the label shows as a tooltip on long press
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureToggleFab(isOverlayRunning: Boolean, onToggleCapture: () -> Unit) {
    val label = stringResource(if (isOverlayRunning) R.string.stop_capture else R.string.start_capture)
    val colors = if (isOverlayRunning) stopCaptureColors else startCaptureColors
    val iconId = if (isOverlayRunning) R.drawable.icon_stop else R.drawable.icon_capture_tile
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = { PlainTooltip { Text(label) } },
        state = rememberTooltipState(),
    ) {
        ScreenFab(colors, onToggleCapture) {
            Icon(painterResource(iconId), label, Modifier.size(24.dp), colors.content)
        }
    }
}
