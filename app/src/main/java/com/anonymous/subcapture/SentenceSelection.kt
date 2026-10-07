package com.anonymous.subcapture

fun chronologicalSelectionText(newestFirstSentences: List<String>, selectedIndices: Set<Int>): String =
    selectedIndices.sortedDescending().joinToString("\n") { newestFirstSentences[it] }

fun toggledSelection(selectedIndices: Set<Int>, index: Int): Set<Int> =
    if (index in selectedIndices) selectedIndices - index else selectedIndices + index
