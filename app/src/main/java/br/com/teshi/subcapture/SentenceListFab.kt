package br.com.teshi.subcapture

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun BoxScope.SentenceListFab(
    isIdle: Boolean,
    isOverlayRunning: Boolean,
    isSendingToSpreadsheet: Boolean,
    onToggleCapture: () -> Unit,
    onSend: () -> Unit,
) {
    AnimatedContent(
        isIdle,
        Modifier.align(Alignment.BottomEnd),
        transitionSpec = { fabSwap() },
        contentAlignment = Alignment.BottomEnd,
        label = "screenFab",
    ) { isShownIdle ->
        if (isShownIdle) CaptureToggleFab(isOverlayRunning, onToggleCapture)
        else SendSelectionButton(isSendingToSpreadsheet, onSend)
    }
}
