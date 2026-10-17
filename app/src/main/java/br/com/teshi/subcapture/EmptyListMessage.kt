package br.com.teshi.subcapture

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EmptyListMessage(isOverlayRunning: Boolean) {
    val hintId = if (isOverlayRunning) R.string.empty_list_hint_running else R.string.empty_list_hint_start
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // Up by half the top bar, so the block centers on the screen rather than on the list below the bar
        Column(
            modifier = Modifier.offset(y = -ScreenTopBarHeight / 2)
                .widthIn(max = 300.dp)
                .padding(horizontal = ScreenGutter),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(painterResource(R.drawable.icon_capture_tile), null, Modifier.size(72.dp), FaintText)
            Text(
                text = stringResource(R.string.empty_sentence_list),
                modifier = Modifier.padding(top = 20.dp),
                color = SecondaryText,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = stringResource(hintId),
                modifier = Modifier.padding(top = 8.dp),
                color = MutedText,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}
