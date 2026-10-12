package br.com.teshi.subcapture

// Not zero: an all-caps track still has cues in normal case, such as the credits of who made it
private const val ALL_CAPS_TRACK_MAX_LOWERCASE_SHARE = 0.1

private val pronounI = Regex("""\bi\b""")
private val initialBeforePeriod = Regex("""\b\p{L}\.""")
private val sentenceStart = Regex("""(^|[.?!])([^\p{L}\p{N}]*)(\p{L})""")

fun sentenceCasedWhenTrackIsAllCaps(cues: List<SubtitleCue>): List<SubtitleCue> {
    val letters = cues.flatMap { cue -> cue.text.filter(Char::isLetter).toList() }
    if (letters.isEmpty()) return cues
    val lowercaseShare = letters.count(Char::isLowerCase).toDouble() / letters.size
    if (lowercaseShare > ALL_CAPS_TRACK_MAX_LOWERCASE_SHARE) return cues
    return cues.map { cue -> if (cue.text.any(Char::isLowerCase)) cue else cue.copy(text = sentenceCased(cue.text)) }
}

fun sentenceCased(allCapsText: String): String =
    allCapsText.lowercase()
        .replace(pronounI, "I")
        .replace(initialBeforePeriod) { it.value.uppercase() }
        .replace(sentenceStart) { it.groupValues[1] + it.groupValues[2] + it.groupValues[3].uppercase() }
