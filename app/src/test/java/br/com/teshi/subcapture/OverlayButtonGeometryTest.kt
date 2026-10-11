package br.com.teshi.subcapture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlayButtonGeometryTest {
    @Test
    fun `movement under 10px on both axes is a tap`() {
        assertTrue(isTapGesture(deltaX = 9f, deltaY = -9f))
    }

    @Test
    fun `movement of 10px on either axis is a drag`() {
        assertFalse(isTapGesture(deltaX = 10f, deltaY = 0f))
        assertFalse(isTapGesture(deltaX = 0f, deltaY = -10f))
    }

    @Test
    fun `a button released within the catch radius of the close target is caught`() {
        assertTrue(isWithinCatchRadius(deltaX = 30, deltaY = -40, catchRadius = 50))
        assertTrue(isWithinCatchRadius(deltaX = 0, deltaY = 0, catchRadius = 50))
    }

    @Test
    fun `a button released past the catch radius is not caught, even when each axis is within it`() {
        assertFalse(isWithinCatchRadius(deltaX = 40, deltaY = 40, catchRadius = 50))
        assertFalse(isWithinCatchRadius(deltaX = -51, deltaY = 0, catchRadius = 50))
    }

    @Test
    fun `button centered on the left half snaps to the left margin`() {
        assertEquals(16, snapTargetX(buttonX = 400, buttonWidth = 100, screenWidth = 1000, edgeMargin = 16))
    }

    @Test
    fun `button centered on the right half snaps to the right margin`() {
        assertEquals(884, snapTargetX(buttonX = 450, buttonWidth = 100, screenWidth = 1000, edgeMargin = 16))
    }
}
