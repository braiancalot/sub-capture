package br.com.teshi.subcapture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SpreadsheetScriptPostTest {
    @Test
    fun `the web app address of a script is accepted`() {
        assertTrue(isSpreadsheetScriptAddress("https://script.google.com/macros/s/AKfycbx_9-Zq/exec"))
    }

    @Test
    fun `the address of a sheet published to the web is refused`() {
        val publishedSheet = "https://docs.google.com/spreadsheets/d/e/2PACX-1vInvented/pubhtml?gid=1&single=true"
        assertFalse(isSpreadsheetScriptAddress(publishedSheet))
    }

    @Test
    fun `the script's answer carries how many rows it wrote`() {
        assertEquals(3, writtenRowCount("""{"written":3}"""))
    }

    @Test
    fun `an error page from a failed script run is not a count`() {
        assertNull(writtenRowCount("<!DOCTYPE html><html><body>TypeError: Cannot read properties</body></html>"))
    }
}
