package br.com.teshi.subcapture

import org.junit.Assert.assertEquals
import org.junit.Test

class SubtitleClosingPunctuationTest {
    @Test
    fun `drops a closing period`() {
        assertEquals("We left the car at home", withoutClosingPeriodOrComma("We left the car at home."))
    }

    @Test
    fun `drops a closing comma`() {
        assertEquals("The dog followed us all the way home", withoutClosingPeriodOrComma("The dog followed us all the way home,"))
    }

    @Test
    fun `keeps a closing question or exclamation mark`() {
        assertEquals("Where is the car?", withoutClosingPeriodOrComma("Where is the car?"))
        assertEquals("Not again!", withoutClosingPeriodOrComma("Not again!"))
    }

    @Test
    fun `keeps a closing ellipsis`() {
        assertEquals("I was thinking...", withoutClosingPeriodOrComma("I was thinking..."))
    }

    @Test
    fun `keeps periods and commas inside the line`() {
        assertEquals("Hi, Phil. Come in", withoutClosingPeriodOrComma("Hi, Phil. Come in."))
    }

    @Test
    fun `a line of only a period becomes empty`() {
        assertEquals("", withoutClosingPeriodOrComma("."))
    }
}
