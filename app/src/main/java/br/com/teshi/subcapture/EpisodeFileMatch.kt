package br.com.teshi.subcapture

private val wordSeparator = Regex("""[^\p{L}\p{N}]+""")

data class VideoFileEntry(val mediaStoreId: Long, val displayName: String)

fun pickEpisodeFile(candidates: List<VideoFileEntry>, playbackTitle: String): VideoFileEntry? {
    val titleWords = wordsOf(playbackTitle)
    return candidates.maxByOrNull { candidate -> wordsOf(candidate.displayName).count { it in titleWords } }
}

private fun wordsOf(text: String): Set<String> =
    text.lowercase().split(wordSeparator).filter { it.isNotEmpty() }.toSet()
