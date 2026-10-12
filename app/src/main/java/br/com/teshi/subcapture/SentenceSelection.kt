package br.com.teshi.subcapture

fun toggledSelection(selectedIndices: Set<Int>, index: Int): Set<Int> =
    if (index in selectedIndices) selectedIndices - index else selectedIndices + index
