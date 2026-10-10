package br.com.teshi.subcapture

const val SUBRIP_CODEC_ID = "S_TEXT/UTF8"
const val ASS_CODEC_ID = "S_TEXT/ASS"

private const val ASS_TEXT_FIELD_INDEX = 8
private val assOverrideBlock = Regex("""\{[^}]*\}""")
private val markupTag = Regex("""<[^>]+>""")
private val assLineBreak = Regex("""\\[Nnh]""")
private val whitespaceRun = Regex("""\s+""")

fun cueTextFromPayload(codecId: String, payload: String): String {
    val rawText = if (codecId == ASS_CODEC_ID) assTextField(payload) else payload
    return rawText
        .replace(assOverrideBlock, "")
        .replace(markupTag, "")
        .replace(assLineBreak, " ")
        .replace(whitespaceRun, " ")
        .trim()
}

private fun assTextField(payload: String): String =
    payload.split(',', limit = ASS_TEXT_FIELD_INDEX + 1).getOrElse(ASS_TEXT_FIELD_INDEX) { "" }
