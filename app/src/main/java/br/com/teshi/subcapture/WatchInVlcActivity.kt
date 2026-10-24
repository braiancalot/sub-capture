package br.com.teshi.subcapture

import android.app.Activity
import android.content.Intent
import android.content.pm.ShortcutManager
import android.os.Bundle
import android.util.Log

class WatchInVlcActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSystemService(ShortcutManager::class.java).reportShortcutUsed(WATCH_IN_VLC_SHORTCUT_ID)
        if (canShowOverlay && canReadVideos) startCaptureAndOpenVlc() else openAppToGrantPermissions()
        finish()
    }

    private fun startCaptureAndOpenVlc() {
        val openVlc = packageManager.getLaunchIntentForPackage(VLC_PACKAGE)
        if (openVlc == null) {
            Log.e(WATCH_IN_VLC_LOG_TAG, "no launcher activity for package $VLC_PACKAGE")
            return showToast(getString(R.string.vlc_not_installed))
        }
        OverlayService.start(this)
        startActivity(openVlc)
    }

    private fun openAppToGrantPermissions() {
        startActivity(Intent(this, MainActivity::class.java))
    }
}
