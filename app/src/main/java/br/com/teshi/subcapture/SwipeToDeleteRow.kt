package br.com.teshi.subcapture

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.drop

private const val DELETE_THRESHOLD_FRACTION = 0.4f

@Composable
fun SwipeToDeleteRow(isEnabled: Boolean, onDelete: () -> Unit, content: @Composable RowScope.() -> Unit) {
    val density = LocalDensity.current
    // Not saveable: the list restores saved state by key, so an undone delete came back already swiped away
    val swipeState = remember {
        SwipeToDismissBoxState(
            SwipeToDismissBoxValue.Settled,
            density,
            positionalThreshold = { totalDistance: Float -> totalDistance * DELETE_THRESHOLD_FRACTION },
        )
    }
    VibrateOnThresholdCrossing(swipeState)
    SwipeToDismissBox(
        state = swipeState,
        backgroundContent = {
            if (swipeState.dismissDirection != SwipeToDismissBoxValue.EndToStart) return@SwipeToDismissBox
            DeleteSwipeBackground(isArmed = swipeState.targetValue == SwipeToDismissBoxValue.EndToStart)
        },
        enableDismissFromStartToEnd = false,
        gesturesEnabled = isEnabled,
        onDismiss = { onDelete() },
        content = content,
    )
}

@Composable
private fun VibrateOnThresholdCrossing(swipeState: SwipeToDismissBoxState) {
    val view = LocalView.current
    LaunchedEffect(swipeState) {
        snapshotFlow { swipeState.targetValue == SwipeToDismissBoxValue.EndToStart }.drop(1).collect { isArmed ->
            val feedback =
                if (isArmed) HapticFeedbackConstants.GESTURE_THRESHOLD_ACTIVATE
                else HapticFeedbackConstants.GESTURE_THRESHOLD_DEACTIVATE
            view.performHapticFeedback(feedback)
        }
    }
}

@Composable
private fun DeleteSwipeBackground(isArmed: Boolean) {
    val backgroundColor by animateColorAsState(if (isArmed) DangerCoral else DangerTint, label = "swipeBackground")
    val iconColor by animateColorAsState(if (isArmed) AppBackground else MutedText, label = "swipeIcon")
    val iconScale by animateFloatAsState(if (isArmed) 1.15f else 0.85f, label = "swipeIconScale")
    val backgroundModifier = Modifier.fillMaxSize().padding(vertical = RowTintInset).clip(RowShape)
        .background(backgroundColor).padding(end = 20.dp)
    Box(backgroundModifier, Alignment.CenterEnd) {
        Icon(painterResource(R.drawable.icon_trash), null, Modifier.size(24.dp).scale(iconScale), iconColor)
    }
}
