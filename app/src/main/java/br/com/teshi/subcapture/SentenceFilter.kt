package br.com.teshi.subcapture

// Taken when the filter opens, so a line marked inside it stays; by capture time, since indices shift
fun unmarkedFilterOf(sentences: List<CapturedSentence>): Set<Long> =
    sentences.filter(::isAwaitingWord).map { it.capturedAtMillis }.toSet()

fun retakenUnmarkedFilter(sentences: List<CapturedSentence>): Set<Long>? =
    unmarkedFilterOf(sentences).ifEmpty { null }

fun settledUnmarkedFilter(sentences: List<CapturedSentence>, filter: Set<Long>?): Set<Long>? {
    if (filter == null || sentences.any { it.capturedAtMillis in filter }) return filter
    return retakenUnmarkedFilter(sentences)
}

fun isLastAwaitingWord(sentences: List<CapturedSentence>, index: Int): Boolean =
    isAwaitingWord(sentences[index]) && sentences.count(::isAwaitingWord) == 1

// Indices into the whole list, so the selection keeps pointing at the same sentences under the filter
fun visibleSentenceIndices(sentences: List<CapturedSentence>, filter: Set<Long>?): List<Int> =
    sentences.indices.filter { filter == null || sentences[it].capturedAtMillis in filter }
