package br.com.teshi.subcapture

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

// The button's height plus its bottom margin: what the list leaves free so its last line clears it
val ScreenFabClearance = 76.dp

class ScreenFabColors(val container: Color, val content: Color)

// Not a FloatingActionButton, which has no long press
@Composable
fun ScreenFab(
    colors: ScreenFabColors,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    isEnabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val fabModifier = Modifier.navigationBarsPadding().padding(end = ScreenGutter, bottom = 20.dp)
    Surface(fabModifier, CircleShape, colors.container, shadowElevation = 12.dp) {
        Row(
            modifier = Modifier.height(56.dp).widthIn(min = 56.dp)
                .combinedClickable(isEnabled, role = Role.Button, onLongClick = onLongClick, onClick = onClick)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}
