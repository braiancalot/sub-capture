package br.com.teshi.subcapture

private const val MAX_STACKED_BUBBLES = 3

class CaptureBubble(val message: String, val isFailure: Boolean, val addedSentence: CapturedSentence?)

fun pushedCaptureBubble(stack: List<CaptureBubble>, bubble: CaptureBubble): List<CaptureBubble> {
    // The earlier bubble is kept because only it carries the sentence that tap added
    val topBubble = stack.find { it.message == bubble.message && it.isFailure == bubble.isFailure } ?: bubble
    return (listOf(topBubble) + withoutCaptureBubble(stack, topBubble)).take(MAX_STACKED_BUBBLES)
}

fun withoutCaptureBubble(stack: List<CaptureBubble>, bubble: CaptureBubble): List<CaptureBubble> =
    stack.filterNot { it === bubble }
