package br.com.teshi.subcapture

import org.junit.Assert.assertEquals
import org.junit.Test

class SentenceSelectionTest {
    @Test
    fun `toggling an unselected index selects it`() {
        assertEquals(setOf(0, 2), toggledSelection(setOf(0), 2))
    }

    @Test
    fun `toggling a selected index deselects it`() {
        assertEquals(setOf(0), toggledSelection(setOf(0, 2), 2))
    }
}
