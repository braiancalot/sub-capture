package br.com.teshi.subcapture

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SentenceStoreTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val sentencesFile: File by lazy { File(temporaryFolder.root, "sentences.txt") }

    @Test
    fun `starts empty when the file does not exist`() {
        assertEquals(emptyList<String>(), SentenceStore(sentencesFile).sentences.value)
    }

    @Test
    fun `reads the list saved by the React Native version`() {
        sentencesFile.writeText("""["newest","say \"hi\""]""")

        assertEquals(listOf("newest", "say \"hi\""), SentenceStore(sentencesFile).sentences.value)
    }

    @Test
    fun `prepend puts the newest sentence first and persists it`() {
        val sentenceStore = SentenceStore(sentencesFile)
        sentenceStore.prepend("first")
        sentenceStore.prepend("second")

        assertEquals(listOf("second", "first"), sentenceStore.sentences.value)
        assertEquals(listOf("second", "first"), SentenceStore(sentencesFile).sentences.value)
    }

    @Test
    fun `removeAt deletes only that position and persists it`() {
        val sentenceStore = SentenceStore(sentencesFile)
        listOf("first", "second", "third").forEach(sentenceStore::prepend)

        sentenceStore.removeAt(1)

        assertEquals(listOf("third", "first"), SentenceStore(sentencesFile).sentences.value)
    }
}
