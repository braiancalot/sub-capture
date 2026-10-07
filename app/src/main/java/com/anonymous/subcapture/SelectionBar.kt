package com.anonymous.subcapture

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val SelectAllText = Color(0xFF90CAF9)

@Composable
fun SelectionBar(selectedCount: Int, onCancel: () -> Unit, onSelectAll: () -> Unit, onCopy: () -> Unit) {
    if (selectedCount == 0) return
    Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
        Row(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onCancel) { Text("Cancelar", color = MutedText, fontSize = 15.sp) }
            TextButton(onClick = onSelectAll) { Text("Tudo", color = SelectAllText, fontSize = 15.sp) }
            Spacer(Modifier.weight(1f))
            Button(onClick = onCopy, shape = RoundedCornerShape(8.dp)) {
                Text("Copiar $selectedCount", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
