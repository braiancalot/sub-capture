package br.com.teshi.subcapture

data class SentenceWord(val start: Int, val endExclusive: Int)

private val wordPattern = Regex("""[\p{L}\p{N}]+(?:['’-][\p{L}\p{N}]+)*""")

fun sentenceWordsOf(sentence: String): List<SentenceWord> =
    wordPattern.findAll(sentence).map { SentenceWord(it.range.first, it.range.last + 1) }.toList()

// The end counts as inside: a tap on the right half of the last letter reports the offset after it
fun wordIndexAt(words: List<SentenceWord>, characterOffset: Int): Int? =
    words.indexOfFirst { characterOffset in it.start..it.endExclusive }.takeIf { it >= 0 }
