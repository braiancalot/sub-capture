package br.com.teshi.subcapture

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class CaptureDay(val date: LocalDate, val captureCount: Int)

enum class RelativeDay { TODAY, YESTERDAY, EARLIER }

fun relativeDayOf(date: LocalDate, today: LocalDate): RelativeDay = when (date) {
    today -> RelativeDay.TODAY
    today.minusDays(1) -> RelativeDay.YESTERDAY
    else -> RelativeDay.EARLIER
}

private fun CapturedSentence.capturedOn(zone: ZoneId): LocalDate =
    Instant.ofEpochMilli(capturedAtMillis).atZone(zone).toLocalDate()

fun dayOpenedAt(newestFirstSentences: List<CapturedSentence>, index: Int, zone: ZoneId): CaptureDay? {
    val date = newestFirstSentences[index].capturedOn(zone)
    if (index > 0 && newestFirstSentences[index - 1].capturedOn(zone) == date) return null
    return CaptureDay(date, newestFirstSentences.count { it.capturedOn(zone) == date })
}
