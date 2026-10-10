package br.com.teshi.subcapture

import org.junit.Assert.assertEquals
import org.junit.Test

class SubtitleCueTextTest {
    @Test
    fun `takes the text field of an ASS event and drops override tags and line breaks`() {
        val payload = """1,0,Main,,0,0,0,,{\i1}It has a forest,\Na river, and a town!{\i0}"""

        assertEquals("It has a forest, a river, and a town!", cueTextFromPayload(ASS_CODEC_ID, payload))
    }

    @Test
    fun `keeps commas inside the ASS text`() {
        assertEquals("Yes, really, truly!", cueTextFromPayload(ASS_CODEC_ID, "3,0,Main,,0,0,0,,Yes, really, truly!"))
    }

    @Test
    fun `an ASS event with fewer fields than expected has no text`() {
        assertEquals("", cueTextFromPayload(ASS_CODEC_ID, "3,0,Main"))
    }

    @Test
    fun `joins SubRip lines and drops markup tags`() {
        val payload = "<i>THE DOG FOLLOWED US</i>\r\nALL THE WAY HOME,"

        assertEquals("THE DOG FOLLOWED US ALL THE WAY HOME,", cueTextFromPayload(SUBRIP_CODEC_ID, payload))
    }

    @Test
    fun `keeps commas in SubRip text, which has no fields`() {
        assertEquals("ONE, TWO, THREE", cueTextFromPayload(SUBRIP_CODEC_ID, "ONE, TWO, THREE"))
    }
}
