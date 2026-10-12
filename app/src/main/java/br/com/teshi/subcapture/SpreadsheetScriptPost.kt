package br.com.teshi.subcapture

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

@Serializable
private data class SpreadsheetRequest(val rows: List<SpreadsheetRow>)

@Serializable
private data class SpreadsheetAnswer(val written: Int)

private val scriptAddressPattern = Regex("""https://script\.google\.com/macros/s/[\w-]+/exec""")

fun isSpreadsheetScriptAddress(address: String): Boolean = scriptAddressPattern.matches(address)

// Apps Script answers a failed run with status 200 and an HTML page: only the count proves the write
fun writtenRowCount(scriptAnswer: String): Int? =
    try {
        Json.decodeFromString(SpreadsheetAnswer.serializer(), scriptAnswer).written
    } catch (notTheScriptAnswer: SerializationException) {
        null
    }

fun postSpreadsheetRows(scriptAddress: String, rows: List<SpreadsheetRow>) {
    val connection = URL(scriptAddress).openConnection() as HttpURLConnection
    try {
        connection.writeRows(rows)
        val scriptAnswer = connection.readAnswer()
        if (writtenRowCount(scriptAnswer) == rows.size) return
        val answerStart = scriptAnswer.take(200)
        throw IOException("spreadsheet script did not confirm ${rows.size} rows, it answered: $answerStart")
    } finally {
        connection.disconnect()
    }
}

private fun HttpURLConnection.writeRows(rows: List<SpreadsheetRow>) {
    // Not measured: chosen long, since a script that sat idle is slow to start its first run
    connectTimeout = 15_000
    readTimeout = 30_000
    doOutput = true
    setRequestProperty("Content-Type", "application/json")
    val requestJson = Json.encodeToString(SpreadsheetRequest.serializer(), SpreadsheetRequest(rows))
    outputStream.use { it.write(requestJson.toByteArray()) }
}

private fun HttpURLConnection.readAnswer(): String {
    if (responseCode != HttpURLConnection.HTTP_OK) {
        throw IOException("spreadsheet script answered HTTP $responseCode, expected 200")
    }
    return inputStream.bufferedReader().use { it.readText() }
}
