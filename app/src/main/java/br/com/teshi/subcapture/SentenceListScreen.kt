package br.com.teshi.subcapture

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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

private val listGutter = ScreenGutter - RowTintOverhang

private val listPadding = PaddingValues(start = listGutter, end = listGutter, bottom = 16.dp + ScreenFabClearance)

@Composable
fun SentenceListScreen(
    sentences: List<CapturedSentence>,
    isOverlayRunning: Boolean,
    isSendingToSpreadsheet: Boolean,
    onToggleCapture: () -> Unit,
    onDeleteSentences: (Set<Int>) -> Unit,
    onToggleMarkedWord: (Int, Int) -> Unit,
    onSendSelection: (Set<Int>) -> Unit,
    onChangeScriptAddress: () -> Unit,
) {
    // Keyed on the list: indices shift when a capture arrives, so a stale selection MUST be dropped
    val selection = remember(sentences) { mutableStateOf(emptySet<Int>()) }
    BackHandler(enabled = selection.value.isNotEmpty()) { selection.value = emptySet() }
    Scaffold { innerPadding ->
        // The buttons float over the list: a Scaffold bottomBar cut the list in a straight line
        Box(Modifier.fillMaxSize().padding(innerPadding).consumeWindowInsets(innerPadding)) {
            Column(Modifier.fillMaxSize()) {
                ScreenTopBar(selection, onDeleteSentences)
                SentenceList(sentences, selection, onToggleMarkedWord) { EmptyListMessage(isOverlayRunning) }
            }
            Box(Modifier.align(Alignment.BottomEnd)) {
                if (selection.value.isEmpty()) return@Box CaptureToggleFab(isOverlayRunning, onToggleCapture)
                SendSelectionButton(
                    isSendingToSpreadsheet,
                    onSend = { onSendSelection(selection.value) },
                    onChangeScriptAddress = onChangeScriptAddress,
                )
            }
        }
    }
}

@Composable
private fun ScreenTopBar(selection: MutableState<Set<Int>>, onDeleteSentences: (Set<Int>) -> Unit) {
    if (selection.value.isEmpty()) return ScreenHeader()
    SelectionTopBar(
        selectedCount = selection.value.size,
        onClear = { selection.value = emptySet() },
        onDelete = { onDeleteSentences(selection.value) },
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
    Box(Modifier.fillMaxWidth().height(ScreenTopBarHeight), Alignment.CenterStart) {
        Text(wordmark, Modifier.padding(horizontal = ScreenGutter), fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SentenceList(
    sentences: List<CapturedSentence>,
    selection: MutableState<Set<Int>>,
    onToggleMarkedWord: (Int, Int) -> Unit,
    emptyList: @Composable () -> Unit,
) {
    if (sentences.isEmpty()) return emptyList()
    val isSelecting = selection.value.isNotEmpty()
    val zone = remember { ZoneId.systemDefault() }
    LazyColumn(contentPadding = listPadding) {
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
