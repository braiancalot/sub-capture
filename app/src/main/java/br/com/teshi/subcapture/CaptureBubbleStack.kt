package br.com.teshi.subcapture

private const val MAX_STACKED_BUBBLES = 3

class CaptureBubble(val message: String, val isFailure: Boolean, val addedSentence: CapturedSentence?)

fun pushedCaptureBubble(stack: List<CaptureBubble>, bubble: CaptureBubble): List<CaptureBubble> {
    val earlier = stack.find { it.message == bubble.message && it.isFailure == bubble.isFailure }
    // The bubble kept is the one carrying the sentence a tap added, so its discard removes that line
    val topBubble = if (earlier != null && bubble.addedSentence == null) earlier else bubble
    val rest = stack.filterNot { it === earlier || it === topBubble }
    return (listOf(topBubble) + rest).take(MAX_STACKED_BUBBLES)
}

fun withoutCaptureBubble(stack: List<CaptureBubble>, bubble: CaptureBubble): List<CaptureBubble> =
    stack.filterNot { it === bubble }
