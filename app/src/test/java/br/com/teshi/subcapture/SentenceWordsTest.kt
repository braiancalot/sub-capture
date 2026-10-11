package br.com.teshi.subcapture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SentenceWordsTest {
    private fun wordTexts(sentence: String): List<String> =
        sentenceWordsOf(sentence).map { sentence.substring(it.start, it.endExclusive) }

    @Test
    fun `punctuation around a word is not part of it`() {
        assertEquals(listOf("Claire", "HONEY", "Phil", "HEY"), wordTexts("Claire: HONEY? Phil: HEY."))
    }

    @Test
    fun `an apostrophe or a hyphen inside a word keeps it whole`() {
        assertEquals(listOf("MA'AM", "don’t", "well-known"), wordTexts("MA'AM, don’t, well-known."))
    }

    @Test
    fun `dashes and the separator between joined lines are not words`() {
        val expected = listOf("AN", "ELECTRICIAN", "HE'LL", "CHARGE", "US")
        assertEquals(expected, wordTexts("AN ELECTRICIAN -- HE'LL / CHARGE US."))
    }

    @Test
    fun `a tap on any character of a word, or on its right edge, finds that word`() {
        val words = sentenceWordsOf("Fired up!")

        assertEquals(0, wordIndexAt(words, 0))
        assertEquals(0, wordIndexAt(words, 5))
        assertEquals(1, wordIndexAt(words, 6))
        assertEquals(1, wordIndexAt(words, 8))
    }

    @Test
    fun `a tap between words or past the last one finds none`() {
        val words = sentenceWordsOf("one -- two!!")

        assertNull(wordIndexAt(words, 5))
        assertNull(wordIndexAt(words, 12))
    }
}
