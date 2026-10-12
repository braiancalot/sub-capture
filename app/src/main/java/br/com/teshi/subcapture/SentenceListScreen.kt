package br.com.teshi.subcapture

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.ZoneId

private fun listPadding(isSelecting: Boolean): PaddingValues {
    val bottomPadding = if (isSelecting) 16.dp + SelectionBarClearance else 16.dp
    return PaddingValues(start = ScreenGutter, end = ScreenGutter, bottom = bottomPadding)
}

@Composable
fun SentenceListScreen(
    sentences: List<CapturedSentence>,
    isOverlayRunning: Boolean,
    onToggleCapture: () -> Unit,
    onDeleteSentences: (Set<Int>) -> Unit,
    onToggleMarkedWord: (Int, Int) -> Unit,
    onCopyText: (String) -> Unit,
) {
    // Keyed on the list: indices shift when a capture arrives, so a stale selection MUST be dropped
    val selection = remember(sentences) { mutableStateOf(emptySet<Int>()) }
    BackHandler(enabled = selection.value.isNotEmpty()) { selection.value = emptySet() }
    Scaffold { innerPadding ->
        // The bar floats over the list: as a Scaffold bottomBar it cut the list in a straight line
        Box(Modifier.fillMaxSize().padding(innerPadding).consumeWindowInsets(innerPadding)) {
            Column(Modifier.fillMaxSize()) {
                ScreenHeader()
                CaptureToggleButton(isOverlayRunning, onToggleCapture)
                SentenceList(sentences, selection, onToggleMarkedWord)
            }
            Box(Modifier.align(Alignment.BottomCenter)) {
                SentenceSelectionBar(sentences, selection, onDeleteSentences, onCopyText)
            }
        }
    }
}

@Composable
private fun SentenceSelectionBar(
    sentences: List<CapturedSentence>,
    selection: MutableState<Set<Int>>,
    onDeleteSentences: (Set<Int>) -> Unit,
    onCopyText: (String) -> Unit,
) {
    SelectionBar(
        selectedCount = selection.value.size,
        onClear = { selection.value = emptySet() },
        onDelete = { onDeleteSentences(selection.value) },
        onCopy = {
            onCopyText(chronologicalSelectionText(sentences.map { it.text }, selection.value))
            selection.value = emptySet()
        },
    )
}

@Composable
private fun ScreenHeader() {
    val plainPart = stringResource(R.string.wordmark_plain_part)
    val accentPart = stringResource(R.string.wordmark_accent_part)
    val wordmark = buildAnnotatedString {
        append(plainPart)
        withStyle(SpanStyle(color = AccentLavender)) { append(accentPart) }
    }
    Text(
        text = wordmark,
        modifier = Modifier.fillMaxWidth().padding(horizontal = ScreenGutter, vertical = 16.dp),
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun CaptureToggleButton(isOverlayRunning: Boolean, onToggleCapture: () -> Unit) {
    Button(
        onClick = onToggleCapture,
        modifier = Modifier.fillMaxWidth().padding(start = ScreenGutter, end = ScreenGutter, bottom = 8.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isOverlayRunning) DangerTint else AccentLavender,
            contentColor = if (isOverlayRunning) DangerCoral else OnAccentInk,
        ),
        contentPadding = PaddingValues(vertical = 14.dp),
    ) {
        Text(
            text = stringResource(if (isOverlayRunning) R.string.stop_capture else R.string.start_capture),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SentenceList(
    sentences: List<CapturedSentence>,
    selection: MutableState<Set<Int>>,
    onToggleMarkedWord: (Int, Int) -> Unit,
) {
    if (sentences.isEmpty()) return EmptyListMessage()
    val isSelecting = selection.value.isNotEmpty()
    val zone = remember { ZoneId.systemDefault() }
    LazyColumn(contentPadding = listPadding(isSelecting), verticalArrangement = Arrangement.spacedBy(CardGap)) {
        itemsIndexed(sentences) { index, sentence ->
            dayOpenedAt(sentences, index, zone)?.let { CaptureDayLabel(it) }
            SentenceRow(
                sentence, isSelecting, index in selection.value,
                onToggleSelected = { selection.value = toggledSelection(selection.value, index) },
                onToggleWord = { wordIndex -> onToggleMarkedWord(index, wordIndex) },
            )
        }
    }
}

@Composable
private fun EmptyListMessage() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(stringResource(R.string.empty_sentence_list), color = MutedText, fontSize = 14.sp)
    }
}
