package br.com.teshi.subcapture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SubtitleLookupTest {
    private val cues = listOf(
        SubtitleCue(1000, 2500, "first"),
        SubtitleCue(3000, 5000, "second"),
        SubtitleCue(4000, 6000, "sign"),
        SubtitleCue(20000, 21000, "late"),
    )

    @Test
    fun `returns the cue on screen at the position`() {
        assertEquals("first", subtitleAt(cues, 1800))
    }

    @Test
    fun `joins cues on screen together, earliest first`() {
        assertEquals("second / sign", subtitleAt(cues, 4500))
    }

    @Test
    fun `a cue is off screen at its end time`() {
        assertEquals("sign", subtitleAt(cues, 5000))
    }

    @Test
    fun `takes a cue that starts within half a second`() {
        assertEquals("first", subtitleAt(cues, 967))
    }

    @Test
    fun `falls back to the cue that left the screen in the last five seconds`() {
        assertEquals("sign", subtitleAt(cues, 9000))
    }

    @Test
    fun `prefers the cue about to start over the one that just ended`() {
        assertEquals("second", subtitleAt(cues, 2700))
    }

    @Test
    fun `returns nothing when no cue is near the position`() {
        assertNull(subtitleAt(cues, 15000))
    }
}
