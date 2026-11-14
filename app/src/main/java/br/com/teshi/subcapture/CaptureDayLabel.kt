package br.com.teshi.subcapture

import android.text.format.DateFormat
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
import java.util.Locale

@Composable
fun CaptureDayLabel(captureDate: LocalDate) {
    val dayName = when (relativeDayOf(captureDate, LocalDate.now())) {
        RelativeDay.TODAY -> stringResource(R.string.day_today)
        RelativeDay.YESTERDAY -> stringResource(R.string.day_yesterday)
        RelativeDay.EARLIER -> shortDateOf(captureDate, LocalDate.now())
    }
    Text(
        text = dayName,
        modifier = Modifier.padding(start = RowTintOverhang, top = 24.dp, bottom = 4.dp),
        color = MutedText,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
    )
}

private fun shortDateOf(date: LocalDate, today: LocalDate): String {
    val skeleton = if (date.year == today.year) "dMMM" else "dMMMy"
    val pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), skeleton)
    return date.format(DateTimeFormatter.ofPattern(pattern))
}
