package br.com.teshi.subcapture

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class MatroskaSubtitleReaderTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `reads the cues of a SubRip track`() {
        val expectedCues = listOf(
            SubtitleCue(1000, 2500, "WE MADE IT!"),
            SubtitleCue(3000, 5000, "THE DOG FOLLOWED US ALL THE WAY HOME,"),
            SubtitleCue(6000, 8000, "IT'S SO QUIET HERE. Dana: MM-HMM. Café?"),
        )

        assertEquals(expectedCues, cuesOfFixture("subrip-track.mkv"))
    }

    @Test
    fun `reads the cues of an ASS track, every style included`() {
        val expectedCues = listOf(
            SubtitleCue(1000, 2500, "Don't say that, Kin'emon!"),
            SubtitleCue(3000, 5000, "It has a forest, a river, and a town!"),
            SubtitleCue(4000, 6000, "Harbor Town East Gate"),
            SubtitleCue(7000, 8000, "Yes, really, truly!"),
        )

        assertEquals(expectedCues, cuesOfFixture("ass-track.mkv"))
    }

    @Test
    fun `reads only the first subtitle track and stays in sync past the blocks of the other`() {
        val expectedCues = listOf(
            SubtitleCue(1000, 2500, "WE MADE IT!"),
            SubtitleCue(3000, 5000, "THE DOG FOLLOWED US ALL THE WAY HOME,"),
            SubtitleCue(6000, 8000, "IT'S SO QUIET HERE. Dana: MM-HMM. Café?"),
        )

        assertEquals(expectedCues, cuesOfFixture("two-subtitle-tracks.mkv"))
    }

    @Test
    fun `names the tracks it found when none is a text subtitle`() {
        val error = assertThrows(IOException::class.java) { cuesOfFixture("no-subtitle-track.mkv") }

        assertTrue(error.message, error.message!!.startsWith("no text subtitle track; tracks found: [V_"))
    }

    @Test
    fun `rejects a file that is not Matroska`() {
        val textFile = temporaryFolder.newFile("notes.mkv").apply { writeText("hello, not a video") }

        val error = assertThrows(IOException::class.java) {
            Files.newByteChannel(textFile.toPath()).use(::readSubtitleCues)
        }

        assertEquals("not a Matroska file: it does not start with an EBML header", error.message)
    }

    private fun cuesOfFixture(fixtureName: String): List<SubtitleCue> {
        val fixturePath = Paths.get(javaClass.getResource("/$fixtureName")!!.toURI())
        return Files.newByteChannel(fixturePath).use(::readSubtitleCues)
    }
}
