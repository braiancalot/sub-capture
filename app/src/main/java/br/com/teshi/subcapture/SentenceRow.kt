package br.com.teshi.subcapture

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp

// How far the selection tint reaches past the text on each side, so the text stays on the gutter
val RowTintOverhang = 8.dp

private val rowCornerRadius = 10.dp
private val rowShape = RoundedCornerShape(rowCornerRadius)
private val textPadding = Modifier.padding(horizontal = RowTintOverhang, vertical = 14.dp)

// Selection only recolors the row: nothing is added to the row, so the text never moves
@Composable
fun SentenceRow(
    sentence: CapturedSentence,
    isSelecting: Boolean,
    isSelected: Boolean,
    onToggleSelected: () -> Unit,
    onToggleWord: (Int) -> Unit,
) {
    val onTapRow = { if (isSelecting) onToggleSelected() }
    val onTapWord = { wordIndex: Int? -> if (isSelecting) onToggleSelected() else wordIndex?.let(onToggleWord) ?: Unit }
    val rowModifier = Modifier.fillMaxWidth().clip(rowShape)
        .selectionTint(isSelected)
        .combinedClickable(onClick = onTapRow, onLongClick = onToggleSelected)
        .rowDivider(isVisible = !isSelected)
    Box(rowModifier) {
        val textModifier = Modifier.fillMaxWidth().then(textPadding)
        MarkableSentenceText(sentence, textModifier, onTapWord, onLongPress = onToggleSelected)
    }
}

// Drawn short of the row's top and bottom, so two selected neighbours do not merge into one block
private fun Modifier.selectionTint(isSelected: Boolean): Modifier = drawBehind {
    if (!isSelected) return@drawBehind
    val inset = 3.dp.toPx()
    val tintSize = Size(size.width, size.height - 2 * inset)
    drawRoundRect(SelectedRowSurface, Offset(0f, inset), tintSize, CornerRadius(rowCornerRadius.toPx()))
}

private fun Modifier.rowDivider(isVisible: Boolean): Modifier = drawBehind {
    if (!isVisible) return@drawBehind
    val thickness = 1.dp.toPx()
    val lineY = size.height - thickness / 2
    val inset = RowTintOverhang.toPx()
    drawLine(RaisedSurface, Offset(inset, lineY), Offset(size.width - inset, lineY), thickness)
}
