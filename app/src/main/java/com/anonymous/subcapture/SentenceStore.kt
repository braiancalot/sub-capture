package com.anonymous.subcapture

import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

private val sentenceListSerializer = ListSerializer(String.serializer())

class SentenceStore(private val sentencesFile: File) {
    private val newestFirstSentences = MutableStateFlow(readSentences())
    val sentences: StateFlow<List<String>> = newestFirstSentences.asStateFlow()

    fun prepend(sentence: String) {
        replaceWith(listOf(sentence) + newestFirstSentences.value)
    }

    fun removeAt(index: Int) {
        replaceWith(newestFirstSentences.value.filterIndexed { position, _ -> position != index })
    }

    private fun replaceWith(updatedSentences: List<String>) {
        newestFirstSentences.value = updatedSentences
        sentencesFile.writeText(Json.encodeToString(sentenceListSerializer, updatedSentences))
    }

    private fun readSentences(): List<String> {
        if (!sentencesFile.exists()) return emptyList()
        val savedJson = sentencesFile.readText()
        if (savedJson.isBlank()) return emptyList()
        return Json.decodeFromString(sentenceListSerializer, savedJson)
    }
}
