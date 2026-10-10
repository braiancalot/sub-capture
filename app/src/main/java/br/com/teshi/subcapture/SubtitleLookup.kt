package br.com.teshi.subcapture

private const val UPCOMING_CUE_TOLERANCE_MILLIS = 500L
private const val RECENT_CUE_WINDOW_MILLIS = 5000L

data class SubtitleCue(val startMillis: Long, val endMillis: Long, val text: String)

fun subtitleAt(cues: List<SubtitleCue>, positionMillis: Long): String? {
    val activeCues = cues.filter { positionMillis >= it.startMillis && positionMillis < it.endMillis }
    if (activeCues.isNotEmpty()) return activeCues.sortedBy { it.startMillis }.joinToString(" / ") { it.text }

    val upcomingCue = cues
        .filter { it.startMillis - positionMillis in 1..UPCOMING_CUE_TOLERANCE_MILLIS }
        .minByOrNull { it.startMillis }
    if (upcomingCue != null) return upcomingCue.text

    return cues
        .filter { positionMillis - it.endMillis in 0..RECENT_CUE_WINDOW_MILLIS }
        .maxByOrNull { it.endMillis }
        ?.text
}
