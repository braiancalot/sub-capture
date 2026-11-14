package br.com.teshi.subcapture

import org.junit.Assert.assertEquals
import org.junit.Test

class CaptureBubbleStackTest {
    private fun bubble(message: String): CaptureBubble = CaptureBubble(message, isFailure = false, addedSentence = null)

    @Test
    fun `pushes the newest bubble on top`() {
        val stack = listOf("first", "second").map(::bubble).fold(emptyList(), ::pushedCaptureBubble)

        assertEquals(listOf("second", "first"), stack.map { it.message })
    }

    @Test
    fun `drops the oldest bubble beyond three`() {
        val stack = listOf("1", "2", "3", "4").map(::bubble).fold(emptyList(), ::pushedCaptureBubble)

        assertEquals(listOf("4", "3", "2"), stack.map { it.message })
    }

    @Test
    fun `moves a bubble with the same message to the top instead of stacking it twice`() {
        val firstTap = CaptureBubble("same", isFailure = false, addedSentence = CapturedSentence("same", 1000))
        val stack = listOf(firstTap, bubble("other"), bubble("same")).fold(emptyList(), ::pushedCaptureBubble)

        assertEquals(listOf("same", "other"), stack.map { it.message })
        assertEquals(true, stack.first() === firstTap)
    }

    @Test
    fun `a repeated tap that added the line again takes the place of the earlier bubble`() {
        val firstTap = CaptureBubble("same", isFailure = false, addedSentence = CapturedSentence("same", 1000))
        val secondTap = CaptureBubble("same", isFailure = false, addedSentence = CapturedSentence("same", 2000))

        val stack = listOf(firstTap, bubble("other"), secondTap).fold(emptyList(), ::pushedCaptureBubble)

        assertEquals(listOf("same", "other"), stack.map { it.message })
        assertEquals(true, stack.first() === secondTap)
    }

    @Test
    fun `removes only the discarded bubble`() {
        val kept = bubble("kept")
        val discarded = bubble("discarded")

        assertEquals(listOf(kept), withoutCaptureBubble(listOf(discarded, kept), discarded))
    }
}
