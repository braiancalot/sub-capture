package br.com.teshi.subcapture

import android.app.PendingIntent
import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class CaptureTileService : TileService() {
    private val tileScope = MainScope()
    private var runningStateJob: Job? = null

    override fun onStartListening() {
        runningStateJob = tileScope.launch { OverlayService.isRunning.collect(::showRunningState) }
    }

    override fun onStopListening() {
        runningStateJob?.cancel()
    }

    override fun onDestroy() {
        tileScope.cancel()
        super.onDestroy()
    }

    override fun onClick() {
        if (OverlayService.isRunning.value) return OverlayService.stop(this)
        if (canShowOverlay && canReadVideos) return OverlayService.start(this)
        openAppToGrantPermissions()
    }

    private fun showRunningState(isRunning: Boolean) {
        val tile = qsTile ?: return
        tile.state = if (isRunning) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.updateTile()
    }

    // A tile cannot show a permission dialog, so the activity's own start flow asks for what is missing
    private fun openAppToGrantPermissions() {
        val openApp = Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivityAndCollapse(PendingIntent.getActivity(this, 0, openApp, PendingIntent.FLAG_IMMUTABLE))
    }
}
