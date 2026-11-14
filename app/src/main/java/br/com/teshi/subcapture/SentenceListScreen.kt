package br.com.teshi.subcapture

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
    onMarkSentencesSent: (Set<Int>) -> Unit,
    onToggleMarkedWord: (Int, Int) -> Unit,
    onSendSelection: (Set<Int>) -> Unit,
    onOpenSettings: () -> Unit,
) {
    // Keyed on the list: indices shift when a capture arrives, so a stale selection MUST be dropped
    val selection = remember(sentences) { mutableStateOf(emptySet<Int>()) }
    BackHandler(enabled = selection.value.isNotEmpty()) { selection.value = emptySet() }
    val filterState = remember { SentenceFilterState() }
    val listState = rememberLazyListState()
    val visibleIndices = visibleSentenceIndices(sentences, filterState.unmarkedFilter.value)
    Scaffold { innerPadding ->
        // The buttons float over the list: a Scaffold bottomBar cut the list in a straight line
        Box(Modifier.fillMaxSize().padding(innerPadding).consumeWindowInsets(innerPadding)) {
            Column(Modifier.fillMaxSize()) {
                ScreenTopBar(selection, onOpenSettings, onMarkSentencesSent) { indices ->
                    filterState.beforeDelete(sentences, indices)
                    onDeleteSentences(indices)
                }
                SentenceFilterBar(sentences, filterState, selection, listState)
                SentenceList(sentences, visibleIndices, selection, listState, { index, wordIndex ->
                    filterState.beforeToggleWord(sentences, index)
                    onToggleMarkedWord(index, wordIndex)
                }) {
                    EmptyListMessage(isOverlayRunning)
                }
            }
            Box(Modifier.align(Alignment.BottomEnd)) {
                if (selection.value.isEmpty()) return@Box CaptureToggleFab(isOverlayRunning, onToggleCapture)
                SendSelectionButton(isSendingToSpreadsheet, onSend = { onSendSelection(selection.value) })
            }
        }
    }
}

@Composable
private fun ScreenTopBar(
    selection: MutableState<Set<Int>>,
    onOpenSettings: () -> Unit,
    onMarkSentencesSent: (Set<Int>) -> Unit,
    onDeleteSentences: (Set<Int>) -> Unit,
) {
    if (selection.value.isEmpty()) return ScreenHeader(onOpenSettings)
    SelectionTopBar(
        selectedCount = selection.value.size,
        onClear = { selection.value = emptySet() },
        onMarkSent = { onMarkSentencesSent(selection.value) },
        onDelete = { onDeleteSentences(selection.value) },
    )
}

@Composable
private fun ScreenHeader(onOpenSettings: () -> Unit) {
    val plainPart = stringResource(R.string.wordmark_plain_part)
    val accentPart = stringResource(R.string.wordmark_accent_part)
    val wordmark = buildAnnotatedString {
        append(plainPart)
        withStyle(SpanStyle(color = AccentLavender)) { append(accentPart) }
    }
    val headerModifier = Modifier.fillMaxWidth().height(ScreenTopBarHeight).padding(start = ScreenGutter, end = 8.dp)
    Row(headerModifier, verticalAlignment = Alignment.CenterVertically) {
        Text(wordmark, Modifier.weight(1f), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        TopBarIconButton(R.drawable.icon_settings, R.string.settings_title, SecondaryText, onOpenSettings)
    }
}

@Composable
private fun SentenceList(
    sentences: List<CapturedSentence>,
    visibleIndices: List<Int>,
    selection: MutableState<Set<Int>>,
    listState: LazyListState,
    onToggleMarkedWord: (Int, Int) -> Unit,
    emptyList: @Composable () -> Unit,
) {
    if (sentences.isEmpty()) return emptyList()
    val isSelecting = selection.value.isNotEmpty()
    val zone = remember { ZoneId.systemDefault() }
    val visibleSentences = visibleIndices.map(sentences::get)
    LazyColumn(state = listState, contentPadding = listPadding) {
        itemsIndexed(visibleIndices) { position, index ->
            dayOpenedAt(visibleSentences, position, zone)?.let { CaptureDayLabel(it) }
            SentenceRow(
                sentences[index], isSelecting, index in selection.value,
                onToggleSelected = { selection.value = toggledSelection(selection.value, index) },
                onToggleWord = { wordIndex -> onToggleMarkedWord(index, wordIndex) },
            )
        }
    }
}
