package br.com.teshi.subcapture

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

private val dialogShape = RoundedCornerShape(24.dp)
private val fieldShape = RoundedCornerShape(10.dp)

@Composable
fun ScriptAddressDialog(savedAddress: String, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var typedAddress by remember { mutableStateOf(savedAddress) }
    val address = typedAddress.trim()
    val isAddressValid = isSpreadsheetScriptAddress(address)
    val isRefused = address.isNotEmpty() && !isAddressValid
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = dialogShape, color = CardSurface) {
            Column(Modifier.padding(24.dp)) {
                ScriptAddressHeading()
                ScriptAddressField(typedAddress, isRefused) { typedAddress = it }
                ScriptAddressActions(canSave = isAddressValid, onSave = { onSave(address) }, onDismiss)
            }
        }
    }
}

@Composable
private fun ScriptAddressHeading() {
    Text(stringResource(R.string.script_address_title), fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
    Text(
        text = stringResource(R.string.script_address_explanation),
        modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
        color = SecondaryText,
        fontSize = 14.sp,
        lineHeight = 21.sp,
    )
}

@Composable
private fun ScriptAddressField(typedAddress: String, isRefused: Boolean, onType: (String) -> Unit) {
    val fieldModifier = Modifier.fillMaxWidth().height(48.dp).clip(fieldShape).background(RaisedSurface)
        .border(1.dp, if (isRefused) DangerCoral else Color.Transparent, fieldShape)
        .padding(horizontal = 14.dp)
    BasicTextField(
        value = typedAddress,
        onValueChange = onType,
        textStyle = LocalTextStyle.current.copy(color = PrimaryText, fontSize = 16.sp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
        singleLine = true,
        cursorBrush = SolidColor(AccentLavender),
        decorationBox = { addressText -> Box(fieldModifier, Alignment.CenterStart) { addressText() } },
    )
}

@Composable
private fun ScriptAddressActions(canSave: Boolean, onSave: () -> Unit, onDismiss: () -> Unit) {
    val saveColors = ButtonDefaults.buttonColors(
        disabledContainerColor = AccentLavender.copy(alpha = 0.4f),
        disabledContentColor = OnAccentInk,
    )
    Row(Modifier.fillMaxWidth().padding(top = 24.dp), Arrangement.spacedBy(8.dp, Alignment.End)) {
        TextButton(onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = SecondaryText)) {
            Text(stringResource(R.string.script_address_cancel), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
        Button(onSave, enabled = canSave, shape = CircleShape, colors = saveColors) {
            Text(stringResource(R.string.script_address_save), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
