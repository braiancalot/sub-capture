package br.com.teshi.subcapture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun SentenceFilterChips(isOnlyUnmarked: Boolean, unmarkedCount: Int, onOnlyUnmarkedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.padding(start = ScreenGutter, end = ScreenGutter, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChoice(stringResource(R.string.filter_all), isSelected = !isOnlyUnmarked) { onOnlyUnmarkedChange(false) }
        FilterChoice(stringResource(R.string.filter_unmarked, unmarkedCount), isSelected = isOnlyUnmarked) {
            onOnlyUnmarkedChange(true)
        }
    }
}

@Composable
private fun FilterChoice(label: String, isSelected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label) },
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
