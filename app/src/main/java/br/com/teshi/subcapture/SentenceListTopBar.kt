package br.com.teshi.subcapture

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SentenceListTopBar(
    selection: MutableState<Set<Int>>,
    onOpenSettings: () -> Unit,
    onMarkSentencesSent: (Set<Int>) -> Unit,
    onDeleteSentences: (Set<Int>) -> Unit,
) {
    // Keyed on emptiness, so the leaving selection bar keeps its count while it fades
    AnimatedContent(selection.value, contentKey = { it.isEmpty() }, transitionSpec = { quickCrossfade() }) { shown ->
        if (shown.isEmpty()) return@AnimatedContent ScreenHeader(onOpenSettings)
        SelectionBarOf(shown.size, selection, onMarkSentencesSent, onDeleteSentences)
    }
}

@Composable
private fun SelectionBarOf(
    selectedCount: Int,
    selection: MutableState<Set<Int>>,
    onMarkSentencesSent: (Set<Int>) -> Unit,
    onDeleteSentences: (Set<Int>) -> Unit,
) {
    SelectionTopBar(
        selectedCount = selectedCount,
        onClear = { selection.value = emptySet() },
        onMarkSent = { onMarkSentencesSent(selection.value) },
        onDelete = { onDeleteSentences(selection.value) },
    )
}

@Composable
private fun ScreenHeader(onOpenSettings: () -> Unit) {
    val plainPart = stringResource(R.string.wordmark_plain_part)
    val accentPart = stringResource(R.string.wordmark_accent_part)
    val wordmark = buildAnnotatedString {
        append(plainPart)
        withStyle(SpanStyle(color = AccentLavender)) { append(accentPart) }
    }
    val headerModifier = Modifier.fillMaxWidth().height(ScreenTopBarHeight).padding(start = ScreenGutter, end = 8.dp)
    Row(headerModifier, verticalAlignment = Alignment.CenterVertically) {
        Text(wordmark, Modifier.weight(1f), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        TopBarIconButton(R.drawable.icon_settings, R.string.settings_title, SecondaryText, onOpenSettings)
    }
}
