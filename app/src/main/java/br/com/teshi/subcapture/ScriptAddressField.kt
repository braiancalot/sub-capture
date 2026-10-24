package br.com.teshi.subcapture

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val fieldShape = RoundedCornerShape(10.dp)
private val fieldInnerPadding = 14.dp

// Saves on every keystroke that leaves a valid address, so there is no save button to forget
@Composable
fun ScriptAddressSetting(savedAddress: String, onSave: (String) -> Unit) {
    var typedAddress by remember { mutableStateOf(savedAddress) }
    val isRefused = typedAddress.isNotBlank() && !isSpreadsheetScriptAddress(typedAddress.trim())
    Column(Modifier.fillMaxWidth().padding(horizontal = ScreenGutter, vertical = 6.dp)) {
        ScriptAddressField(typedAddress, isRefused) { address ->
            typedAddress = address
            if (isSpreadsheetScriptAddress(address.trim())) onSave(address.trim())
        }
        Text(
            text = stringResource(R.string.settings_script_address_hint),
            modifier = Modifier.padding(start = fieldInnerPadding, top = 6.dp),
            color = if (isRefused) DangerCoral else MutedText,
            fontSize = 13.sp,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScriptAddressField(typedAddress: String, isRefused: Boolean, onType: (String) -> Unit) {
    val focusManager = LocalFocusManager.current
    val isKeyboardOpen = WindowInsets.isImeVisible
    // Back closes the keyboard without leaving the field, which would keep the cursor blinking
    LaunchedEffect(isKeyboardOpen) { if (!isKeyboardOpen) focusManager.clearFocus() }
    val fieldModifier = Modifier.fillMaxWidth().clip(fieldShape).background(RaisedSurface)
        .border(1.dp, if (isRefused) DangerCoral else Color.Transparent, fieldShape)
        .padding(horizontal = fieldInnerPadding, vertical = 10.dp)
    BasicTextField(
        value = typedAddress,
        onValueChange = onType,
        textStyle = LocalTextStyle.current.copy(color = PrimaryText, fontSize = 16.sp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        singleLine = true,
        cursorBrush = SolidColor(AccentLavender),
        decorationBox = { addressText ->
            Column(fieldModifier) {
                Text(stringResource(R.string.settings_script_address), color = MutedText, fontSize = 12.sp)
                Box(Modifier.padding(top = 2.dp)) { addressText() }
            }
        },
    )
}
