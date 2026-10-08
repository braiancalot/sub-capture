package br.com.teshi.subcapture

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

fun subtitleFromBlocks(blockTexts: List<String>): String? {
    val flattenedBlocks = blockTexts.map { it.replace("\n", " ").trim() }.filter { it.isNotEmpty() }
    if (flattenedBlocks.isEmpty()) return null
    return flattenedBlocks.joinToString(" / ")
}

class SubtitleRecognizer {
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun recognize(frame: Bitmap, onSubtitle: (String?) -> Unit, onFailure: (String) -> Unit) {
        textRecognizer.process(InputImage.fromBitmap(frame, 0))
            .addOnSuccessListener { text -> onSubtitle(subtitleFromBlocks(text.textBlocks.map { it.text })) }
            .addOnFailureListener { error -> onFailure("ML Kit text recognition failed: $error") }
            .addOnCompleteListener { frame.recycle() }
    }

    fun close() {
        textRecognizer.close()
    }
}
