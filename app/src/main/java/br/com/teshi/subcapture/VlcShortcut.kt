package br.com.teshi.subcapture

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Icon
import android.util.Log

const val WATCH_IN_VLC_SHORTCUT_ID = "watch_in_vlc"
const val WATCH_IN_VLC_LOG_TAG = "WatchInVlc"

// Published from code because a static shortcut names the package, which differs in the debug build
fun Context.publishWatchInVlcShortcut() {
    val watchInVlc = ShortcutInfo.Builder(this, WATCH_IN_VLC_SHORTCUT_ID)
        .setShortLabel(getString(R.string.shortcut_vlc_with_capture))
        .setIcon(watchInVlcIcon())
        .setIntent(Intent(Intent.ACTION_VIEW, null, this, WatchInVlcActivity::class.java))
        .build()
    getSystemService(ShortcutManager::class.java).dynamicShortcuts = listOf(watchInVlc)
}

val Context.isVlcShortcutPinned: Boolean
    get() = getSystemService(ShortcutManager::class.java).pinnedShortcuts.any { it.id == WATCH_IN_VLC_SHORTCUT_ID }

// Xiaomi's launcher shrinks a resource icon into a white plate, but draws an adaptive bitmap edge to edge
private fun Context.watchInVlcIcon(): Icon {
    val adaptiveIcon = getDrawable(R.drawable.shortcut_watch_in_vlc) as AdaptiveIconDrawable
    val sizePx = (108 * resources.displayMetrics.density).toInt()
    val iconBitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(iconBitmap)
    for (layer in listOf(adaptiveIcon.background, adaptiveIcon.foreground)) {
        layer.setBounds(0, 0, sizePx, sizePx)
        layer.draw(canvas)
    }
    return Icon.createWithAdaptiveBitmap(iconBitmap)
}

fun Context.requestVlcShortcutPin() {
    val shortcutManager = getSystemService(ShortcutManager::class.java)
    val pinnedShortcut = ShortcutInfo.Builder(this, WATCH_IN_VLC_SHORTCUT_ID).build()
    val isPinSupported = shortcutManager.isRequestPinShortcutSupported
    if (isPinSupported && shortcutManager.requestPinShortcut(pinnedShortcut, null)) return
    Log.e(WATCH_IN_VLC_LOG_TAG, "launcher refused to pin $WATCH_IN_VLC_SHORTCUT_ID, pin supported: $isPinSupported")
    showToast(getString(R.string.shortcut_pin_refused))
}

// Xiaomi ignores a pin request without its own shortcut permission and still reports success
private fun Context.xiaomiPermissionEditorIntent(): Intent =
    Intent("miui.intent.action.APP_PERM_EDITOR").putExtra("extra_pkgname", packageName)

val Context.hasXiaomiPermissionEditor: Boolean
    get() = packageManager.resolveActivity(xiaomiPermissionEditorIntent(), 0) != null

fun Context.openXiaomiPermissionEditor() {
    startActivity(xiaomiPermissionEditorIntent())
}
