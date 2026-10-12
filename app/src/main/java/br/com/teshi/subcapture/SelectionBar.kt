package br.com.teshi.subcapture

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// The bar's height plus its bottom margin: what the list leaves free so its last line clears the bar
val SelectionBarClearance = 84.dp

private val barMargin = Modifier.padding(start = ScreenGutter, end = ScreenGutter, bottom = 20.dp)
// A Button keeps 4dp of touch space above and below itself, so the sides need 4dp more to match
private val barContentPadding = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)

// The list fades into the background under the bar: a shadow does not show on a near-black screen
private val listFadeHeight = SelectionBarClearance + 56.dp
private val listFade = Brush.verticalGradient(0f to AppBackground.copy(alpha = 0f), 0.5f to AppBackground)

@Composable
fun SelectionBar(
    selectedCount: Int,
    onClear: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
) {
    if (selectedCount == 0) return
    val barModifier = Modifier.navigationBarsPadding().then(barMargin).fillMaxWidth()
    Box(contentAlignment = Alignment.BottomCenter) {
        Box(Modifier.fillMaxWidth().height(listFadeHeight).background(listFade))
        Surface(barModifier, CircleShape, RaisedSurface, shadowElevation = 12.dp) {
            Row(barContentPadding, Arrangement.spacedBy(8.dp), Alignment.CenterVertically) {
                SelectionCount(selectedCount, onClear)
                Spacer(Modifier.weight(1f))
                DeleteSelectedButton(onDelete)
                CopySelectedButton(onCopy)
            }
        }
    }
}

@Composable
private fun CopySelectedButton(onCopy: () -> Unit) {
    Button(onCopy, shape = CircleShape, contentPadding = PaddingValues(horizontal = 18.dp)) {
        Icon(painterResource(R.drawable.icon_copy), null, Modifier.padding(end = 8.dp).size(18.dp))
        Text(stringResource(R.string.selection_copy), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SelectionCount(selectedCount: Int, onClear: () -> Unit) {
    Row(
        modifier = Modifier.clip(CircleShape).clickable(onClick = onClear).padding(end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(48.dp), Alignment.Center) {
            val clearLabel = stringResource(R.string.selection_clear)
            Icon(painterResource(R.drawable.icon_close), clearLabel, Modifier.size(24.dp), SecondaryText)
        }
        Text("$selectedCount", color = PrimaryText, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun DeleteSelectedButton(onDelete: () -> Unit) {
    Box(
        modifier = Modifier.size(48.dp).clip(CircleShape).clickable(onClick = onDelete),
        contentAlignment = Alignment.Center,
    ) {
        val deleteLabel = stringResource(R.string.delete_selected)
        Icon(painterResource(R.drawable.icon_trash), deleteLabel, Modifier.size(24.dp), DangerCoral)
    }
}
