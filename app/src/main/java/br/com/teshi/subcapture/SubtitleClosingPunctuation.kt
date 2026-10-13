package br.com.teshi.subcapture

private const val ELLIPSIS_ENDING = ".."

fun withoutClosingPeriodOrComma(cueText: String): String =
    if (cueText.endsWith(ELLIPSIS_ENDING)) cueText else cueText.trimEnd('.', ',')
