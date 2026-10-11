package br.com.teshi.subcapture

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun CaptureDayLabel(captureDay: CaptureDay) {
    val dayName = when (relativeDayOf(captureDay.date, LocalDate.now())) {
        RelativeDay.TODAY -> stringResource(R.string.day_today)
        RelativeDay.YESTERDAY -> stringResource(R.string.day_yesterday)
        RelativeDay.EARLIER -> captureDay.date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
    }
    Text(
        text = stringResource(R.string.day_with_capture_count, dayName, captureDay.captureCount).uppercase(),
        modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 8.dp),
        color = MutedText,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.sp,
    )
}
