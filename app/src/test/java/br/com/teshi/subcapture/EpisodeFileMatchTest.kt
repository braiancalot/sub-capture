package br.com.teshi.subcapture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EpisodeFileMatchTest {
    private val zou02 = VideoFileEntry(1, "[One Pace][804-805] Zou 02 [720p][32150FF4].mkv")
    private val zou03 = VideoFileEntry(2, "[One Pace][806-807] Zou 03 [720p][E4B077F8].mkv")
    private val holidayClip = VideoFileEntry(3, "VID_20260101_120000.mp4")

    @Test
    fun `picks the file whose name shares the most words with the title VLC cleaned up`() {
        val pickedFile = pickEpisodeFile(listOf(holidayClip, zou03, zou02), "[One Pace][804 805] Zou 02 [32150FF4]")

        assertEquals(zou02, pickedFile)
    }

    @Test
    fun `ignores letter case and punctuation`() {
        val rashDecisions = VideoFileEntry(4, "Modern Family (2009) - S06E13 - Rash Decisions (1080p WEB-DL).mkv")
        val bigGuns = VideoFileEntry(5, "Modern Family (2009) - S06E12 - The Big Guns (1080p WEB-DL).mkv")

        val pickedFile = pickEpisodeFile(listOf(bigGuns, rashDecisions), "modern family (2009) s06e13 rash decisions")

        assertEquals(rashDecisions, pickedFile)
    }

    @Test
    fun `takes the only file of that duration even when the title shares no word with it`() {
        assertEquals(holidayClip, pickEpisodeFile(listOf(holidayClip), "Some embedded title"))
    }

    @Test
    fun `finds nothing when no file has that duration`() {
        assertNull(pickEpisodeFile(emptyList(), "[One Pace][804 805] Zou 02 [32150FF4]"))
    }
}
