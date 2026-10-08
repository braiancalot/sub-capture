package br.com.teshi.subcapture

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val rowShape = RoundedCornerShape(8.dp)

@Composable
fun SentenceRow(
    sentence: String,
    isSelecting: Boolean,
    isSelected: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onDelete: () -> Unit,
) {
    val rowModifier = Modifier.fillMaxWidth().clip(rowShape)
        .combinedClickable(onClick = onTap, onLongClick = onLongPress)
    val rowColor = if (isSelected) SelectedRowBackground else MaterialTheme.colorScheme.surface
    val rowBorder = BorderStroke(1.dp, if (isSelected) CaptureBlue else Color.Transparent)
    Surface(rowModifier, rowShape, rowColor, border = rowBorder) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            if (isSelecting) SelectionCheckbox(isSelected)
            Text(sentence, modifier = Modifier.weight(1f), fontSize = 16.sp, lineHeight = 22.sp)
            if (!isSelecting) DeleteButton(onDelete)
        }
    }
}

@Composable
private fun SelectionCheckbox(isSelected: Boolean) {
    Box(
        modifier = Modifier
            .padding(end = 12.dp)
            .size(22.dp)
            .background(if (isSelected) CaptureBlue else Color.Transparent, CircleShape)
            .border(2.dp, if (isSelected) CaptureBlue else FaintText, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) Text("✓", fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DeleteButton(onDelete: () -> Unit) {
    Text(
        text = "✕",
        modifier = Modifier.clip(CircleShape).clickable(onClick = onDelete).padding(12.dp),
        color = FaintText,
        fontSize = 14.sp,
    )
}
