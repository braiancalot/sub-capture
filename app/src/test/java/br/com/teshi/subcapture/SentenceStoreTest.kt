package br.com.teshi.subcapture

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SentenceStoreTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val sentencesFile: File by lazy { File(temporaryFolder.root, "captures.json") }

    private fun savedTexts(): List<String> = SentenceStore(sentencesFile).sentences.value.map { it.text }

    @Test
    fun `starts empty when the file does not exist`() {
        assertEquals(emptyList<CapturedSentence>(), SentenceStore(sentencesFile).sentences.value)
    }

    @Test
    fun `reads a saved list with escaped quotes and no marked words`() {
        sentencesFile.writeText("""[{"text":"say \"hi\"","capturedAtMillis":1700000000000}]""")

        val expected = listOf(CapturedSentence("say \"hi\"", 1700000000000, emptySet()))
        assertEquals(expected, SentenceStore(sentencesFile).sentences.value)
    }

    @Test
    fun `prepend puts the newest sentence first and persists it with its capture time`() {
        val sentenceStore = SentenceStore(sentencesFile)
        sentenceStore.prepend("first", capturedAtMillis = 1000)
        sentenceStore.prepend("second", capturedAtMillis = 2000)

        val expected = listOf(CapturedSentence("second", 2000), CapturedSentence("first", 1000))
        assertEquals(expected, sentenceStore.sentences.value)
        assertEquals(expected, SentenceStore(sentencesFile).sentences.value)
    }

    @Test
    fun `prepend ignores a sentence that is already in the list`() {
        val sentenceStore = SentenceStore(sentencesFile)
        listOf("first", "second", "first").forEach { sentenceStore.prepend(it, capturedAtMillis = 1000) }

        assertEquals(listOf("second", "first"), sentenceStore.sentences.value.map { it.text })
    }

    @Test
    fun `prepend returns the sentence it added, and null for one already in the list`() {
        val sentenceStore = SentenceStore(sentencesFile)

        assertEquals(CapturedSentence("first", 1000), sentenceStore.prepend("first", capturedAtMillis = 1000))
        assertEquals(null, sentenceStore.prepend("first", capturedAtMillis = 2000))
    }

    @Test
    fun `discard deletes the capture with that text and time, even after its words were marked`() {
        val sentenceStore = SentenceStore(sentencesFile)
        sentenceStore.prepend("first", capturedAtMillis = 1000)
        val added = sentenceStore.prepend("second", capturedAtMillis = 2000)!!
        sentenceStore.toggleMarkedWord(index = 0, wordIndex = 0)

        sentenceStore.discard(added)

        assertEquals(listOf("first"), savedTexts())
    }

    @Test
    fun `discard leaves a line that was captured again at another time`() {
        val sentenceStore = SentenceStore(sentencesFile)
        sentenceStore.prepend("first", capturedAtMillis = 1000)

        sentenceStore.discard(CapturedSentence("first", 2000))

        assertEquals(listOf("first"), savedTexts())
    }

    @Test
    fun `toggleMarkedWord marks and unmarks words of one sentence and persists them`() {
        val sentenceStore = SentenceStore(sentencesFile)
        listOf("first", "second").forEach { sentenceStore.prepend(it, capturedAtMillis = 1000) }

        listOf(2, 0, 2, 3).forEach { sentenceStore.toggleMarkedWord(index = 1, wordIndex = it) }

        val saved = SentenceStore(sentencesFile).sentences.value
        assertEquals(listOf(emptySet(), setOf(0, 3)), saved.map { it.markedWordIndices })
    }

    @Test
    fun `removeAll deletes only those positions and persists it`() {
        val sentenceStore = SentenceStore(sentencesFile)
        listOf("first", "second", "third", "fourth").forEach { sentenceStore.prepend(it, capturedAtMillis = 1000) }

        sentenceStore.removeAll(setOf(0, 2))

        assertEquals(listOf("third", "first"), savedTexts())
    }

    @Test
    fun `restore puts a deleted line back by its capture time, with its marked words`() {
        val sentenceStore = SentenceStore(sentencesFile)
        listOf("first", "second", "third", "fourth").forEachIndexed { position, text ->
            sentenceStore.prepend(text, capturedAtMillis = 1000L * (position + 1))
        }
        sentenceStore.toggleMarkedWord(index = 2, wordIndex = 0)
        val deleted = sentenceStore.sentences.value[2]
        sentenceStore.removeAll(setOf(2, 0))

        sentenceStore.restore(deleted)

        assertEquals(listOf("third", "second", "first"), savedTexts())
        assertEquals(setOf(0), SentenceStore(sentencesFile).sentences.value[1].markedWordIndices)
    }

    @Test
    fun `restore puts the oldest line last`() {
        val sentenceStore = SentenceStore(sentencesFile)
        sentenceStore.prepend("newer", capturedAtMillis = 2000)

        sentenceStore.restore(CapturedSentence("older", 1000))

        assertEquals(listOf("newer", "older"), savedTexts())
    }

    @Test
    fun `restore ignores a line whose text was captured again meanwhile`() {
        val sentenceStore = SentenceStore(sentencesFile)
        sentenceStore.prepend("first", capturedAtMillis = 2000)

        sentenceStore.restore(CapturedSentence("first", 1000))

        assertEquals(listOf(CapturedSentence("first", 2000)), sentenceStore.sentences.value)
    }

    @Test
    fun `markSent flags the lines with those texts and persists it`() {
        val sentenceStore = SentenceStore(sentencesFile)
        listOf("first", "second", "third").forEach { sentenceStore.prepend(it, capturedAtMillis = 1000) }

        sentenceStore.markSent(setOf("first", "third"))

        val saved = SentenceStore(sentencesFile).sentences.value
        assertEquals(listOf(true, false, true), saved.map { it.isSentToSpreadsheet })
    }
}
