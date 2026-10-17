package br.com.teshi.subcapture

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Shared by the wordmark and the selection bar, so the list does not jump when one replaces the other
val ScreenTopBarHeight = 64.dp

@Composable
fun SelectionTopBar(selectedCount: Int, onClear: () -> Unit, onDelete: () -> Unit) {
    val barModifier = Modifier.fillMaxWidth().height(ScreenTopBarHeight).padding(horizontal = 8.dp)
    Row(barModifier, verticalAlignment = Alignment.CenterVertically) {
        SelectionIconButton(R.drawable.icon_close, R.string.selection_clear, SecondaryText, onClear)
        Spacer(Modifier.width(4.dp))
        Text(
            text = pluralStringResource(R.plurals.selection_count, selectedCount, selectedCount),
            color = PrimaryText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            // Tabular digits keep the word after the count still while the count changes
            style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum"),
        )
        Spacer(Modifier.weight(1f))
        SelectionIconButton(R.drawable.icon_trash, R.string.delete_selected, DangerCoral, onDelete)
    }
}

@Composable
private fun SelectionIconButton(iconId: Int, labelId: Int, tint: Color, onClick: () -> Unit) {
    Box(Modifier.size(48.dp).clip(CircleShape).clickable(onClick = onClick), Alignment.Center) {
        Icon(painterResource(iconId), stringResource(labelId), Modifier.size(24.dp), tint)
    }
}

private val sendColors = ScreenFabColors(AccentLavender, OnAccentInk)

// The long press is how the script address is changed
@Composable
fun SendSelectionButton(isSending: Boolean, onSend: () -> Unit, onChangeScriptAddress: () -> Unit) {
    ScreenFab(sendColors, onSend, onLongClick = onChangeScriptAddress, isEnabled = !isSending) {
        SendProgressOrIcon(isSending, Modifier.padding(start = 4.dp, end = 10.dp).size(20.dp))
        Text(
            text = stringResource(R.string.selection_send),
            modifier = Modifier.padding(end = 4.dp),
            color = OnAccentInk,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun SendProgressOrIcon(isSending: Boolean, modifier: Modifier) {
    if (!isSending) return Icon(painterResource(R.drawable.icon_send), null, modifier, OnAccentInk)
    CircularProgressIndicator(modifier, OnAccentInk, strokeWidth = 2.dp, trackColor = Color.Transparent)
}
