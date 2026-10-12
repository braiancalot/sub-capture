package br.com.teshi.subcapture

import android.content.Context
import android.util.Log
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val LOG_TAG = "SpreadsheetSend"

suspend fun Context.sendSelectionToSpreadsheet(scriptAddress: String, selectedIndices: Set<Int>) {
    val toSend = sentencesToSend(sentenceStore.sentences.value, selectedIndices)
    if (toSend.isEmpty()) return showToast(getString(R.string.spreadsheet_nothing_to_send))
    val rows = toSend.flatMap(::spreadsheetRowsOf)
    try {
        withContext(Dispatchers.IO) { postSpreadsheetRows(scriptAddress, rows) }
    } catch (sendFailure: IOException) {
        Log.w(LOG_TAG, "${rows.size} rows of ${toSend.size} lines not sent: $sendFailure")
        return showToast(getString(R.string.spreadsheet_send_failed))
    }
    sentenceStore.markSent(toSend.map { it.text }.toSet())
    showToast(sentLinesMessage(toSend.size, skippedCount = selectedIndices.size - toSend.size))
}

private fun Context.sentLinesMessage(sentCount: Int, skippedCount: Int): String =
    if (skippedCount == 0) getString(R.string.spreadsheet_sentences_sent, sentCount)
    else getString(R.string.spreadsheet_sentences_sent_and_skipped, sentCount, skippedCount)
