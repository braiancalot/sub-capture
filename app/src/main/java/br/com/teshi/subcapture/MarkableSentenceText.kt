package br.com.teshi.subcapture

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.sp

// Color only: a heavier weight widens the word and pushes the rest of the line
private val markedWordStyle = SpanStyle(color = AccentLavender, background = AccentTint)

@Composable
fun MarkableSentenceText(
    sentence: CapturedSentence,
    modifier: Modifier,
    onTapWord: (Int?) -> Unit,
    onLongPress: () -> Unit,
) {
    val words = remember(sentence.text) { sentenceWordsOf(sentence.text) }
    val textLayout = remember { mutableStateOf<TextLayoutResult?>(null) }
    val onTapAt = { position: Offset -> onTapWord(tappedWordIndex(textLayout.value, words, position)) }
    Text(
        text = markedSentenceText(sentence, words),
        modifier = modifier.tapAndLongPress(onTapAt, onLongPress),
        fontSize = 17.sp,
        lineHeight = 26.sp,
        onTextLayout = { textLayout.value = it },
    )
}

@Composable
private fun Modifier.tapAndLongPress(onTap: (Offset) -> Unit, onLongPress: () -> Unit): Modifier {
    val currentOnTap by rememberUpdatedState(onTap)
    val currentOnLongPress by rememberUpdatedState(onLongPress)
    return pointerInput(Unit) {
        detectTapGestures(onLongPress = { currentOnLongPress() }, onTap = { currentOnTap(it) })
    }
}

private fun markedSentenceText(sentence: CapturedSentence, words: List<SentenceWord>): AnnotatedString =
    buildAnnotatedString {
        append(sentence.text)
        sentence.markedWordIndices.mapNotNull(words::getOrNull).forEach {
            addStyle(markedWordStyle, it.start, it.endExclusive)
        }
    }

private fun tappedWordIndex(textLayout: TextLayoutResult?, words: List<SentenceWord>, position: Offset): Int? {
    if (textLayout == null) return null
    val line = textLayout.getLineForVerticalPosition(position.y)
    // Past the end of a line the nearest offset is its last word, which the tap did not touch
    if (position.x > textLayout.getLineRight(line)) return null
    return wordIndexAt(words, textLayout.getOffsetForPosition(position))
}
