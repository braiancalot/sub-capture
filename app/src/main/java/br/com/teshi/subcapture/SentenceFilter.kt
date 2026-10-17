package br.com.teshi.subcapture

// Taken when the filter opens, so a line marked inside it stays; by capture time, since indices shift
fun unmarkedFilterOf(sentences: List<CapturedSentence>): Set<Long> =
    sentences.filter(::isAwaitingWord).map { it.capturedAtMillis }.toSet()

// Indices into the whole list, so the selection keeps pointing at the same sentences under the filter
fun visibleSentenceIndices(sentences: List<CapturedSentence>, filter: Set<Long>?): List<Int> =
    sentences.indices.filter { filter == null || sentences[it].capturedAtMillis in filter }
