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
}
