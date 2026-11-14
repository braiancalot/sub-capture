package br.com.teshi.subcapture

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    onRestoreSentence: (CapturedSentence) -> Unit,
    onMarkSentencesSent: (Set<Int>) -> Unit,
    onToggleMarkedWord: (Int, Int) -> Unit,
    onSendSelection: (Set<Int>) -> Unit,
    onOpenSettings: () -> Unit,
) {
    // Keyed on the list: indices shift when a capture arrives, so a stale selection MUST be dropped
    val selection = remember(sentences) { mutableStateOf(emptySet<Int>()) }
    BackHandler(enabled = selection.value.isNotEmpty()) { selection.value = emptySet() }
    val filterState = remember { SentenceFilterState() }
    val snackbarHostState = remember { SnackbarHostState() }
    val deleteSentences = { indices: Set<Int> ->
        filterState.beforeDelete(sentences, indices)
        onDeleteSentences(indices)
    }
    val deleteWithUndo = rememberUndoableDelete(snackbarHostState, onRestoreSentence) { index ->
        sentences[index].also { deleteSentences(setOf(index)) }
    }
    Scaffold { innerPadding ->
        // The buttons float over the list: a Scaffold bottomBar cut the list in a straight line
        Box(Modifier.fillMaxSize().padding(innerPadding).consumeWindowInsets(innerPadding)) {
            Column(Modifier.fillMaxSize()) {
                SentenceListTopBar(selection, onOpenSettings, onMarkSentencesSent, deleteSentences)
                FilteredSentenceList(sentences, filterState, selection, isOverlayRunning, deleteWithUndo, onToggleMarkedWord)
            }
            UndoDeleteSnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter).padding(bottom = ScreenFabClearance))
            val onSend = { onSendSelection(selection.value) }
            SentenceListFab(selection.value.isEmpty(), isOverlayRunning, isSendingToSpreadsheet, onToggleCapture, onSend)
        }
    }
}

@Composable
private fun FilteredSentenceList(
    sentences: List<CapturedSentence>,
    filterState: SentenceFilterState,
    selection: MutableState<Set<Int>>,
    isOverlayRunning: Boolean,
    onSwipeDelete: (Int) -> Unit,
    onToggleMarkedWord: (Int, Int) -> Unit,
) {
    val listState = rememberLazyListState()
    SentenceFilterBar(sentences, filterState, selection, listState)
    val visibleIndices = visibleSentenceIndices(sentences, filterState.unmarkedFilter.value)
    val toggleWord = { index: Int, wordIndex: Int ->
        filterState.beforeToggleWord(sentences, index)
        onToggleMarkedWord(index, wordIndex)
    }
    val emptyListMessage: @Composable () -> Unit = { EmptyListMessage(isOverlayRunning) }
    SentenceList(sentences, visibleIndices, selection, listState, onSwipeDelete, toggleWord, emptyListMessage)
}

@Composable
private fun SentenceList(
    sentences: List<CapturedSentence>,
    visibleIndices: List<Int>,
    selection: MutableState<Set<Int>>,
    listState: LazyListState,
    onSwipeDelete: (Int) -> Unit,
    onToggleMarkedWord: (Int, Int) -> Unit,
    emptyList: @Composable () -> Unit,
) {
    if (sentences.isEmpty()) return emptyList()
    val isSelecting = selection.value.isNotEmpty()
    val zone = remember { ZoneId.systemDefault() }
    val visibleSentences = visibleIndices.map(sentences::get)
    LazyColumn(state = listState, contentPadding = listPadding) {
        // Keyed by text, unique in the list, so a row's swipe state never passes to the row that takes its place
        itemsIndexed(visibleIndices, key = { _, index -> sentences[index].text }) { position, index ->
            Column(Modifier.animateItem()) {
                dayOpenedAt(visibleSentences, position, zone)?.let { CaptureDayLabel(it) }
                SentenceRow(
                    sentences[index], isSelecting, index in selection.value,
                    onToggleSelected = { selection.value = toggledSelection(selection.value, index) },
                    onToggleWord = { wordIndex -> onToggleMarkedWord(index, wordIndex) },
                    onSwipeDelete = { onSwipeDelete(index) },
                )
            }
        }
    }
}
