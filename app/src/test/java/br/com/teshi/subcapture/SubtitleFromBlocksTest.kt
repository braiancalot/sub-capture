package br.com.teshi.subcapture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SubtitleFromBlocksTest {
    @Test
    fun `joins blocks with a slash separator`() {
        assertEquals("Hello there / General Kenobi", subtitleFromBlocks(listOf("Hello there", "General Kenobi")))
    }

    @Test
    fun `flattens line breaks inside a block`() {
        assertEquals("first line second line", subtitleFromBlocks(listOf("first line\nsecond line")))
    }

    @Test
    fun `drops blank blocks`() {
        assertEquals("only one", subtitleFromBlocks(listOf("  ", "only one", "\n")))
    }

    @Test
    fun `returns null when no text was recognized`() {
        assertNull(subtitleFromBlocks(emptyList()))
        assertNull(subtitleFromBlocks(listOf(" ")))
    }
}
