package br.com.teshi.subcapture

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val barMargin = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 20.dp)
private val barContentPadding = Modifier.padding(start = 14.dp, top = 8.dp, end = 8.dp, bottom = 8.dp)

@Composable
fun SelectionBar(
    selectedCount: Int,
    onClear: () -> Unit,
    onSelectAll: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
) {
    if (selectedCount == 0) return
    val barModifier = Modifier.navigationBarsPadding().then(barMargin).fillMaxWidth()
    Surface(barModifier, CircleShape, RaisedSurface, shadowElevation = 12.dp) {
        Row(barContentPadding, Arrangement.spacedBy(8.dp), Alignment.CenterVertically) {
            SelectionCount(selectedCount, onClear)
            Spacer(Modifier.weight(1f))
            SelectAllButton(onSelectAll)
            DeleteSelectedButton(onDelete)
            Button(onCopy, shape = CircleShape, contentPadding = PaddingValues(horizontal = 18.dp)) {
                Text(stringResource(R.string.selection_copy), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun SelectionCount(selectedCount: Int, onClear: () -> Unit) {
    Row(
        modifier = Modifier.clip(CircleShape).clickable(onClick = onClear).padding(end = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.defaultMinSize(minWidth = 26.dp).height(26.dp)
                .background(AccentLavender, CircleShape).padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("$selectedCount", color = OnAccentInk, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        val isSingle = selectedCount == 1
        val selectedLabelId = if (isSingle) R.string.selection_one_selected else R.string.selection_several_selected
        Text(stringResource(selectedLabelId), color = SecondaryText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SelectAllButton(onSelectAll: () -> Unit) {
    Text(
        text = stringResource(R.string.selection_select_all),
        modifier = Modifier.clip(CircleShape).clickable(onClick = onSelectAll).padding(10.dp),
        color = AccentLavender,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
    )
}

@Composable
private fun DeleteSelectedButton(onDelete: () -> Unit) {
    Box(
        modifier = Modifier.size(40.dp).clip(CircleShape).background(DangerTint).clickable(onClick = onDelete),
        contentAlignment = Alignment.Center,
    ) {
        val deleteLabel = stringResource(R.string.delete_selected)
        Icon(painterResource(R.drawable.icon_trash), deleteLabel, Modifier.size(19.dp), DangerCoral)
    }
}
