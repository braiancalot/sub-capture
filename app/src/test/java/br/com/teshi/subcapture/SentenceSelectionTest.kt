package br.com.teshi.subcapture

import org.junit.Assert.assertEquals
import org.junit.Test

class SentenceSelectionTest {
    private val newestFirstSentences = listOf("third", "second", "first")

    @Test
    fun `copied text lists the selection oldest first`() {
        assertEquals("first\nthird", chronologicalSelectionText(newestFirstSentences, setOf(0, 2)))
    }

    @Test
    fun `single selection copies just that sentence`() {
        assertEquals("second", chronologicalSelectionText(newestFirstSentences, setOf(1)))
    }

    @Test
    fun `toggling an unselected index selects it`() {
        assertEquals(setOf(0, 2), toggledSelection(setOf(0), 2))
    }

    @Test
    fun `toggling a selected index deselects it`() {
        assertEquals(setOf(0), toggledSelection(setOf(0, 2), 2))
    }
}
