package br.com.teshi.subcapture

import org.junit.Assert.assertEquals
import org.junit.Test

class SentenceFilterTest {
    private val sentences = listOf(
        CapturedSentence("Bell!", capturedAtMillis = 3000),
        CapturedSentence("Moo Golova!", capturedAtMillis = 2000, markedWordIndices = setOf(1)),
        CapturedSentence("Cam. What?", capturedAtMillis = 1000, isSentToSpreadsheet = true),
        CapturedSentence("Don't get in our way!", capturedAtMillis = 500),
    )

    @Test
    fun `the filter takes the unsent sentences without a marked word`() {
        assertEquals(setOf(3000L, 500L), unmarkedFilterOf(sentences))
    }

    @Test
    fun `all sentences show, in list order, without a filter`() {
        assertEquals(listOf(0, 1, 2, 3), visibleSentenceIndices(sentences, filter = null))
    }

    @Test
    fun `a sentence marked after the filter was taken still shows`() {
        val filter = setOf(3000L, 2000L)
        assertEquals(listOf(0, 1), visibleSentenceIndices(sentences, filter))
    }

    @Test
    fun `a capture arriving after the filter was taken stays out of it`() {
        val withNewCapture = listOf(CapturedSentence("New line", capturedAtMillis = 4000)) + sentences
        assertEquals(listOf(1, 4), visibleSentenceIndices(withNewCapture, unmarkedFilterOf(sentences)))
    }

    @Test
    fun `retaking the filter brings new captures and drops the lines marked since`() {
        val markedSince = sentences.map { if (it.capturedAtMillis == 500L) it.copy(markedWordIndices = setOf(0)) else it }
        val withNewCapture = listOf(CapturedSentence("New line", capturedAtMillis = 4000)) + markedSince
        assertEquals(setOf(4000L, 3000L), retakenUnmarkedFilter(withNewCapture))
    }

    @Test
    fun `retaking the filter closes it when nothing awaits a word`() {
        val allMarked = sentences.map { it.copy(markedWordIndices = setOf(0)) }
        assertEquals(null, retakenUnmarkedFilter(allMarked))
    }

    @Test
    fun `only the single line awaiting a word is the last one`() {
        val oneAwaiting = sentences.drop(1).map { if (it.capturedAtMillis == 500L) it else it.copy(markedWordIndices = setOf(0)) }
        assertEquals(true, isLastAwaitingWord(oneAwaiting, index = 2))
        assertEquals(false, isLastAwaitingWord(oneAwaiting, index = 0))
        assertEquals(false, isLastAwaitingWord(sentences, index = 0))
    }

    @Test
    fun `a closed filter stays closed when settled`() {
        assertEquals(null, settledUnmarkedFilter(sentences, filter = null))
    }

    @Test
    fun `a filter still showing a line is kept as it is`() {
        assertEquals(setOf(2000L), settledUnmarkedFilter(sentences, filter = setOf(2000L)))
    }

    @Test
    fun `a filter emptied by a delete is taken again from what still awaits a word`() {
        assertEquals(setOf(3000L, 500L), settledUnmarkedFilter(sentences, filter = setOf(9000L)))
    }

    @Test
    fun `a filter emptied by a delete closes when nothing awaits a word`() {
        assertEquals(null, settledUnmarkedFilter(emptyList(), filter = setOf(9000L)))
    }
}
