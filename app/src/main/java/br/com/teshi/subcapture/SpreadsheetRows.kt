package br.com.teshi.subcapture

import kotlinx.serialization.Serializable

@Serializable
data class SpreadsheetRow(val word: String, val example: String)

fun sentencesToSend(newestFirstSentences: List<CapturedSentence>, selectedIndices: Set<Int>): List<CapturedSentence> =
    selectedIndices.sortedDescending().map { newestFirstSentences[it] }
        .filter { !it.isSentToSpreadsheet && it.markedWordIndices.isNotEmpty() }

fun spreadsheetRowsOf(sentence: CapturedSentence): List<SpreadsheetRow> {
    val words = sentenceWordsOf(sentence.text)
    val markedIndices = sentence.markedWordIndices.filter { it in words.indices }.sorted()
    return adjacentRuns(markedIndices).map { run ->
        // Cut from the line, punctuation included, so the sheet's formatter finds it in the example
        val expression = sentence.text.substring(words[run.first()].start, words[run.last()].endExclusive)
        SpreadsheetRow(expression, sentence.text)
    }
}

private fun adjacentRuns(sortedIndices: List<Int>): List<List<Int>> =
    sortedIndices.fold(emptyList<List<Int>>()) { runs, index ->
        val openRun = runs.lastOrNull()
        if (openRun?.last() == index - 1) runs.dropLast(1).plusElement(openRun + index)
        else runs.plusElement(listOf(index))
    }
