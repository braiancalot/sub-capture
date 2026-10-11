package br.com.teshi.subcapture

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class CaptureDaysTest {
    private val saoPaulo = ZoneId.of("America/Sao_Paulo")

    private fun capturedAt(dateTime: String): CapturedSentence {
        val capturedAtMillis = LocalDateTime.parse(dateTime).atZone(saoPaulo).toInstant().toEpochMilli()
        return CapturedSentence(dateTime, capturedAtMillis)
    }

    private val newestFirstSentences = listOf(
        capturedAt("2026-10-11T00:05:00"),
        capturedAt("2026-10-10T23:55:00"),
        capturedAt("2026-10-10T08:00:00"),
        capturedAt("2026-10-08T12:00:00"),
    )

    @Test
    fun `the first capture and each first capture of an older day open a day group`() {
        val openers = newestFirstSentences.indices.map { dayOpenedAt(newestFirstSentences, it, saoPaulo) }

        val expected = listOf(
            CaptureDay(LocalDate.of(2026, 10, 11), captureCount = 1),
            CaptureDay(LocalDate.of(2026, 10, 10), captureCount = 2),
            null,
            CaptureDay(LocalDate.of(2026, 10, 8), captureCount = 1),
        )
        assertEquals(expected, openers)
    }

    @Test
    fun `a day is named relative to today only for today and the day before`() {
        val today = LocalDate.of(2026, 10, 11)

        assertEquals(RelativeDay.TODAY, relativeDayOf(LocalDate.of(2026, 10, 11), today))
        assertEquals(RelativeDay.YESTERDAY, relativeDayOf(LocalDate.of(2026, 10, 10), today))
        assertEquals(RelativeDay.EARLIER, relativeDayOf(LocalDate.of(2026, 10, 9), today))
    }

    @Test
    fun `the day follows the given time zone, not UTC`() {
        val lateEvening = listOf(capturedAt("2026-10-10T22:30:00"))

        assertEquals(LocalDate.of(2026, 10, 10), dayOpenedAt(lateEvening, 0, saoPaulo)?.date)
        assertEquals(LocalDate.of(2026, 10, 11), dayOpenedAt(lateEvening, 0, ZoneId.of("UTC"))?.date)
    }
}
