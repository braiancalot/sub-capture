package br.com.teshi.subcapture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.ZoneId

@Composable
fun SentenceListScreen(
    sentences: List<CapturedSentence>,
    isOverlayRunning: Boolean,
    onToggleCapture: () -> Unit,
    onDeleteSentence: (Int) -> Unit,
    onToggleMarkedWord: (Int, Int) -> Unit,
    onCopyText: (String) -> Unit,
) {
    // Keyed on the list: indices shift when a capture arrives, so a stale selection MUST be dropped
    val selection = remember(sentences) { mutableStateOf(emptySet<Int>()) }
    val toggleSelected = { index: Int -> selection.value = toggledSelection(selection.value, index) }
    Scaffold(bottomBar = { SentenceSelectionBar(sentences, selection, onCopyText) }) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            ScreenHeader()
            CaptureToggleButton(isOverlayRunning, onToggleCapture)
            SentenceList(sentences, selection.value, toggleSelected, onDeleteSentence, onToggleMarkedWord)
        }
    }
}

@Composable
private fun SentenceSelectionBar(
    sentences: List<CapturedSentence>,
    selection: MutableState<Set<Int>>,
    onCopyText: (String) -> Unit,
) {
    SelectionBar(
        selectedCount = selection.value.size,
        onCancel = { selection.value = emptySet() },
        onSelectAll = { selection.value = sentences.indices.toSet() },
        onCopy = {
            onCopyText(chronologicalSelectionText(sentences.map { it.text }, selection.value))
            selection.value = emptySet()
        },
    )
}

@Composable
private fun ScreenHeader() {
    Text(
        text = "SubCapture",
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun CaptureToggleButton(isOverlayRunning: Boolean, onToggleCapture: () -> Unit) {
    Button(
        onClick = onToggleCapture,
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isOverlayRunning) StopRed else CaptureBlue,
            contentColor = Color.White,
        ),
        contentPadding = PaddingValues(vertical = 14.dp),
    ) {
        Text(
            text = stringResource(if (isOverlayRunning) R.string.stop_capture else R.string.start_capture),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun SentenceList(
    sentences: List<CapturedSentence>,
    selectedIndices: Set<Int>,
    onToggleSelected: (Int) -> Unit,
    onDeleteSentence: (Int) -> Unit,
    onToggleMarkedWord: (Int, Int) -> Unit,
) {
    if (sentences.isEmpty()) return EmptyListMessage()
    val isSelecting = selectedIndices.isNotEmpty()
    val zone = remember { ZoneId.systemDefault() }
    LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        itemsIndexed(sentences) { index, sentence ->
            dayOpenedAt(sentences, index, zone)?.let { CaptureDayLabel(it) }
            SentenceRow(
                sentence, isSelecting, index in selectedIndices,
                onTap = { if (isSelecting) onToggleSelected(index) },
                onLongPress = { onToggleSelected(index) },
                onToggleWord = { wordIndex -> onToggleMarkedWord(index, wordIndex) },
                onDelete = { onDeleteSentence(index) },
            )
        }
    }
}

@Composable
private fun EmptyListMessage() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(stringResource(R.string.empty_sentence_list), color = FaintText, fontSize = 14.sp)
    }
}
