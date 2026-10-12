package br.com.teshi.subcapture

import org.junit.Assert.assertEquals
import org.junit.Test

class SubtitleLetterCaseTest {
    private fun cuesOf(vararg texts: String): List<SubtitleCue> =
        texts.mapIndexed { index, text -> SubtitleCue(index * 1000L, index * 1000L + 900, text) }

    private fun textsAfterCasing(vararg texts: String): List<String> =
        sentenceCasedWhenTrackIsAllCaps(cuesOf(*texts)).map { it.text }

    @Test
    fun `an all-caps line becomes a sentence and the pronoun I stays capital`() {
        val expected = "I'll be sure to send you that recording of my bassoon solo."
        assertEquals(expected, sentenceCased("I'LL BE SURE TO SEND YOU THAT RECORDING OF MY BASSOON SOLO."))
    }

    @Test
    fun `each sentence of a line starts with a capital, behind a dialogue dash too`() {
        assertEquals("-Hey. -What? I think I'm late!", sentenceCased("-HEY. -WHAT? I THINK I'M LATE!"))
    }

    @Test
    fun `a decimal point starts no sentence and an abbreviation keeps its capitals`() {
        assertEquals("It costs 3.5 dollars in the U.S.A.", sentenceCased("IT COSTS 3.5 DOLLARS IN THE U.S.A."))
    }

    @Test
    fun `the lines of an all-caps track are cased and its normal-case credits are left alone`() {
        val episodeLines = Array(20) { "HE LEFT. I SAW HIM GO." }

        val cased = textsAfterCasing(*episodeLines, "WHERE IS PHIL?", "Subtitles by someone")

        assertEquals(List(20) { "He left. I saw him go." } + "Where is phil?" + "Subtitles by someone", cased)
    }

    @Test
    fun `a shouted line in a normal-case track is left alone`() {
        val texts = arrayOf("Where is Phil?", "NO WAY!", "He left, I saw him go.")

        assertEquals(texts.toList(), textsAfterCasing(*texts))
    }

    @Test
    fun `a track without cues stays empty`() {
        assertEquals(emptyList<String>(), textsAfterCasing())
    }
}
