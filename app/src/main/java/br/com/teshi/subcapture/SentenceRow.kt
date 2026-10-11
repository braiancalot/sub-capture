package br.com.teshi.subcapture

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val rowShape = RoundedCornerShape(CardRadius)

// Selection only recolors the card: nothing is added to the row, so the text never moves
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
        .combinedClickable(onClick = onTapRow, onLongClick = onToggleSelected)
    val rowColor = if (isSelected) SelectedCardSurface else CardSurface
    val rowBorder = BorderStroke(1.dp, if (isSelected) AccentLine else Color.Transparent)
    Surface(rowModifier, rowShape, rowColor, border = rowBorder) {
        val textModifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)
        MarkableSentenceText(sentence, textModifier, onTapWord, onLongPress = onToggleSelected)
    }
}
