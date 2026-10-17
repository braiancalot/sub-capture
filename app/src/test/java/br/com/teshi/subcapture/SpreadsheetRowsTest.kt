package br.com.teshi.subcapture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpreadsheetRowsTest {
    private fun sentence(text: String, vararg markedWordIndices: Int, isSent: Boolean = false): CapturedSentence =
        CapturedSentence(text, capturedAtMillis = 1000, markedWordIndices.toSet(), isSent)

    @Test
    fun `an unsent line without a marked word is awaiting a word`() {
        assertTrue(isAwaitingWord(sentence("Bell!")))
    }

    @Test
    fun `a marked or an already sent line is not awaiting a word`() {
        assertFalse(isAwaitingWord(sentence("Moo Golova!", 1)))
        assertFalse(isAwaitingWord(sentence("Cam. What?", isSent = true)))
    }

    @Test
    fun `a marked word becomes one row with the line as its example`() {
        val expected = listOf(SpreadsheetRow("wrist", "He wore a bracelet on his wrist."))
        assertEquals(expected, spreadsheetRowsOf(sentence("He wore a bracelet on his wrist.", 6)))
    }

    @Test
    fun `adjacent marked words become one expression`() {
        val expected = listOf(SpreadsheetRow("set sail", "The ship will set sail tomorrow."))
        assertEquals(expected, spreadsheetRowsOf(sentence("The ship will set sail tomorrow.", 4, 3)))
    }

    @Test
    fun `marked words apart become one row each, in the order of the line`() {
        val line = "Stop frowning and muttering at me."
        val expected = listOf(SpreadsheetRow("frowning", line), SpreadsheetRow("muttering", line))
        assertEquals(expected, spreadsheetRowsOf(sentence(line, 3, 1)))
    }

    @Test
    fun `a line without a marked word gives no row`() {
        assertEquals(emptyList<SpreadsheetRow>(), spreadsheetRowsOf(sentence("Nothing marked here.")))
    }

    @Test
    fun `a marked position past the last word is ignored`() {
        assertEquals(listOf(SpreadsheetRow("up", "Fired up!")), spreadsheetRowsOf(sentence("Fired up!", 1, 7)))
    }

    @Test
    fun `the selection is sent oldest first, without sent lines and lines with no marked word`() {
        val newestFirstSentences = listOf(
            sentence("newest", 0),
            sentence("not marked"),
            sentence("already sent", 0, isSent = true),
            sentence("oldest", 0),
            sentence("not selected", 0),
        )

        val toSend = sentencesToSend(newestFirstSentences, setOf(0, 1, 2, 3))

        assertEquals(listOf("oldest", "newest"), toSend.map { it.text })
    }
}
