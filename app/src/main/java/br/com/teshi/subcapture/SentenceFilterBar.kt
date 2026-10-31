package br.com.teshi.subcapture

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

class SentenceFilterState {
    val unmarkedFilter = mutableStateOf<Set<Long>?>(null)
    val isCompletionPending = mutableStateOf(false)

    fun beforeToggleWord(sentences: List<CapturedSentence>, index: Int) {
        isCompletionPending.value = isLastAwaitingWord(sentences, index)
    }

    fun beforeDelete(sentences: List<CapturedSentence>, indices: Set<Int>) {
        val remaining = sentences.filterIndexed { position, _ -> position !in indices }
        unmarkedFilter.value = settledUnmarkedFilter(remaining, unmarkedFilter.value)
    }
}

private data class ListPosition(val index: Int, val scrollOffset: Int)

@Composable
fun SentenceFilterBar(
    sentences: List<CapturedSentence>,
    filterState: SentenceFilterState,
    selection: MutableState<Set<Int>>,
    listState: LazyListState,
) {
    val unmarkedFilter = filterState.unmarkedFilter
    val positionBeforeFilter = remember { mutableStateOf<ListPosition?>(null) }
    RetakeFilterOnReturn(sentences, unmarkedFilter, selection)
    ListPositionAcrossFilter(unmarkedFilter.value != null, listState, positionBeforeFilter)
    val unmarkedCount = sentences.count(::isAwaitingWord)
    SentenceFilterChips(unmarkedFilter.value != null, unmarkedCount, filterState.isCompletionPending) { isOnlyUnmarked ->
        if (isOnlyUnmarked) positionBeforeFilter.value = listState.currentPosition()
        unmarkedFilter.value = if (isOnlyUnmarked) unmarkedFilterOf(sentences) else null
        // A selection MUST NOT outlive the filter, or a delete would reach lines out of sight
        selection.value = emptySet()
    }
}

@Composable
private fun RetakeFilterOnReturn(
    sentences: List<CapturedSentence>,
    unmarkedFilter: MutableState<Set<Long>?>,
    selection: MutableState<Set<Int>>,
) {
    val returnCount = remember { mutableIntStateOf(0) }
    LifecycleEventEffect(Lifecycle.Event.ON_START) { returnCount.intValue++ }
    // Through a recomposition: a stopped screen composes nothing, so its sentences are old at ON_START
    LaunchedEffect(returnCount.intValue) {
        val filter = unmarkedFilter.value ?: return@LaunchedEffect
        val retaken = retakenUnmarkedFilter(sentences)
        if (retaken == filter) return@LaunchedEffect
        unmarkedFilter.value = retaken
        selection.value = emptySet()
    }
}

@Composable
private fun ListPositionAcrossFilter(
    isFiltering: Boolean,
    listState: LazyListState,
    positionBeforeFilter: MutableState<ListPosition?>,
) {
    LaunchedEffect(isFiltering) {
        if (isFiltering) return@LaunchedEffect listState.scrollToItem(0)
        val position = positionBeforeFilter.value ?: return@LaunchedEffect
        listState.scrollToItem(position.index, position.scrollOffset)
        positionBeforeFilter.value = null
    }
}

private fun LazyListState.currentPosition(): ListPosition =
    ListPosition(firstVisibleItemIndex, firstVisibleItemScrollOffset)
