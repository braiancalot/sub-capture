package com.anonymous.subcapture

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
    fun `button centered on the left half snaps to the left margin`() {
        assertEquals(16, snapTargetX(buttonX = 400, buttonWidth = 100, screenWidth = 1000, edgeMargin = 16))
    }

    @Test
    fun `button centered on the right half snaps to the right margin`() {
        assertEquals(884, snapTargetX(buttonX = 450, buttonWidth = 100, screenWidth = 1000, edgeMargin = 16))
    }
}
