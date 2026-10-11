package br.com.teshi.subcapture

import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Serializable
data class CapturedSentence(
    val text: String,
    val capturedAtMillis: Long,
    val markedWordIndices: Set<Int> = emptySet(),
)

private val sentenceListSerializer = ListSerializer(CapturedSentence.serializer())

class SentenceStore(private val sentencesFile: File) {
    private val newestFirstSentences = MutableStateFlow(readSentences())
    val sentences: StateFlow<List<CapturedSentence>> = newestFirstSentences.asStateFlow()

    fun prepend(text: String, capturedAtMillis: Long) {
        if (newestFirstSentences.value.any { it.text == text }) return
        replaceWith(listOf(CapturedSentence(text, capturedAtMillis)) + newestFirstSentences.value)
    }

    fun removeAll(indices: Set<Int>) {
        replaceWith(newestFirstSentences.value.filterIndexed { position, _ -> position !in indices })
    }

    fun toggleMarkedWord(index: Int, wordIndex: Int) {
        val sentence = newestFirstSentences.value[index]
        val marked = sentence.copy(markedWordIndices = toggledSelection(sentence.markedWordIndices, wordIndex))
        replaceWith(newestFirstSentences.value.toMutableList().also { it[index] = marked })
    }

    private fun replaceWith(updatedSentences: List<CapturedSentence>) {
        newestFirstSentences.value = updatedSentences
        sentencesFile.writeText(Json.encodeToString(sentenceListSerializer, updatedSentences))
    }

    private fun readSentences(): List<CapturedSentence> {
        if (!sentencesFile.exists()) return emptyList()
        val savedJson = sentencesFile.readText()
        if (savedJson.isBlank()) return emptyList()
        return Json.decodeFromString(sentenceListSerializer, savedJson)
    }
}
