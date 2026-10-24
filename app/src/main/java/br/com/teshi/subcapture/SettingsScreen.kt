package br.com.teshi.subcapture

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DisabledText = PrimaryText.copy(alpha = 0.38f)

@Composable
fun SettingsScreen(
    savedScriptAddress: String,
    onSaveScriptAddress: (String) -> Unit,
    isVlcShortcutPinned: Boolean,
    onAddVlcShortcut: () -> Unit,
    onOpenShortcutPermission: (() -> Unit)?,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)
    val focusManager = LocalFocusManager.current
    val screenModifier = Modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
    Scaffold(screenModifier) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            SettingsTopBar(onBack)
            SettingsSectionHeader(R.string.settings_section_spreadsheet)
            ScriptAddressSetting(savedScriptAddress, onSaveScriptAddress)
            SettingsSectionHeader(R.string.settings_section_shortcut)
            VlcShortcutSettings(isVlcShortcutPinned, onAddVlcShortcut, onOpenShortcutPermission)
        }
    }
}

@Composable
private fun SettingsTopBar(onBack: () -> Unit) {
    val barModifier = Modifier.fillMaxWidth().height(ScreenTopBarHeight).padding(horizontal = 8.dp)
    Row(barModifier, verticalAlignment = Alignment.CenterVertically) {
        TopBarIconButton(R.drawable.icon_back, R.string.settings_back, SecondaryText, onBack)
        Spacer(Modifier.width(4.dp))
        Text(stringResource(R.string.settings_title), color = PrimaryText, fontSize = 18.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SettingsSectionHeader(labelId: Int) {
    Text(
        text = stringResource(labelId).uppercase(),
        modifier = Modifier.padding(start = ScreenGutter, end = ScreenGutter, top = 24.dp, bottom = 8.dp),
        color = AccentLavender,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.8.sp,
    )
}

@Composable
private fun VlcShortcutSettings(isPinned: Boolean, onAdd: () -> Unit, onOpenPermission: (() -> Unit)?) {
    if (isPinned) return SettingsRow(R.string.settings_vlc_shortcut, R.string.settings_vlc_shortcut_pinned, null)
    SettingsRow(R.string.settings_vlc_shortcut, R.string.settings_vlc_shortcut_hint, onAdd)
    if (onOpenPermission == null) return
    SettingsRow(R.string.settings_xiaomi_shortcut_permission, R.string.settings_xiaomi_shortcut_permission_hint, onOpenPermission)
}

@Composable
private fun SettingsRow(titleId: Int, hintId: Int, onClick: (() -> Unit)?) {
    val rowModifier = if (onClick == null) Modifier else Modifier.clickable(onClick = onClick)
    Column(rowModifier.fillMaxWidth().padding(horizontal = ScreenGutter, vertical = 14.dp)) {
        Text(stringResource(titleId), color = if (onClick == null) DisabledText else PrimaryText, fontSize = 16.sp)
        Text(stringResource(hintId), Modifier.padding(top = 2.dp), color = MutedText, fontSize = 14.sp)
    }
}
