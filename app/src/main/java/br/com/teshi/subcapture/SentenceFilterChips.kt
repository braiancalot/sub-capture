package br.com.teshi.subcapture

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

// Long enough to read the completion before the chips fold away under it
private const val ALL_MARKED_HOLD_MILLIS = 1100L

@Composable
fun SentenceFilterChips(
    isOnlyUnmarked: Boolean,
    unmarkedCount: Int,
    isCompletionPending: MutableState<Boolean>,
    onOnlyUnmarkedChange: (Boolean) -> Unit,
) {
    val isAllMarked = unmarkedCount == 0 && !isOnlyUnmarked
    val isShown = remember { mutableStateOf(!isAllMarked) }
    LaunchedEffect(isAllMarked) {
        if (!isAllMarked) {
            isCompletionPending.value = false
            isShown.value = true
            return@LaunchedEffect
        }
        if (isCompletionPending.value) delay(ALL_MARKED_HOLD_MILLIS)
        isShown.value = false
    }
    AnimatedVisibility(isShown.value, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
        Row(Modifier.padding(start = ScreenGutter, end = ScreenGutter, bottom = 4.dp)) {
            if (isAllMarked && isCompletionPending.value) return@Row AllMarkedBadge()
            UnmarkedToggle(isOnlyUnmarked, unmarkedCount) { onOnlyUnmarkedChange(!isOnlyUnmarked) }
        }
    }
}

@Composable
private fun AllMarkedBadge() {
    Row(
        // As tall as a chip's touch area, or the row shrinks the moment the chips turn into it
        modifier = Modifier.minimumInteractiveComponentSize().height(32.dp).background(AccentTint, CircleShape)
            .padding(start = 8.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CheckIcon()
        Text(stringResource(R.string.filter_all_marked), color = AccentLavender, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun CheckIcon() {
    Icon(painterResource(R.drawable.icon_check), null, Modifier.size(FilterChipDefaults.IconSize), AccentLavender)
}

@Composable
private fun CloseIcon() {
    Icon(painterResource(R.drawable.icon_close), null, Modifier.size(FilterChipDefaults.IconSize), AccentLavender)
}

@Composable
private fun UnmarkedToggle(isOn: Boolean, unmarkedCount: Int, onClick: () -> Unit) {
    val isFinished = isOn && unmarkedCount == 0
    val label = if (isFinished) stringResource(R.string.filter_all_marked) else stringResource(R.string.filter_unmarked, unmarkedCount)
    FilterChip(
        selected = isOn,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = if (isFinished) ({ CheckIcon() }) else null,
        trailingIcon = if (isOn) ({ CloseIcon() }) else null,
        shape = CircleShape,
        // Filled like every other surface of the app, which draws no outlines
        border = null,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = RaisedSurface,
            selectedContainerColor = AccentTint,
            selectedLabelColor = AccentLavender,
            labelColor = SecondaryText,
        ),
    )
}
