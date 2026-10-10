package br.com.teshi.subcapture

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.browse.MediaBrowser
import android.media.session.MediaController
import android.media.session.PlaybackState
import android.util.Log

private val VLC_PLAYBACK_SERVICE = ComponentName("org.videolan.vlc", "org.videolan.vlc.PlaybackService")

data class VlcPlayback(val title: String, val durationMillis: Long, val positionMillis: Long)

class VlcConnection(private val context: Context, private val onVideoChanged: () -> Unit) {
    private var mediaBrowser: MediaBrowser? = null
    private var mediaController: MediaController? = null

    private val metadataCallback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) {
            onVideoChanged()
        }
    }

    private val connectionCallback = object : MediaBrowser.ConnectionCallback() {
        override fun onConnected() {
            mediaController = mediaBrowser?.let { MediaController(context, it.sessionToken) }
            mediaController?.registerCallback(metadataCallback)
            if (context.isDebuggableBuild) Log.d(CAPTURE_LOG_TAG, "connected to $VLC_PLAYBACK_SERVICE")
            onVideoChanged()
        }

        override fun onConnectionSuspended() {
            mediaController = null
            Log.e(CAPTURE_LOG_TAG, "connection to $VLC_PLAYBACK_SERVICE suspended, VLC's service stopped")
        }

        override fun onConnectionFailed() {
            Log.e(CAPTURE_LOG_TAG, "connection to $VLC_PLAYBACK_SERVICE refused, or VLC is not installed")
        }
    }

    val isConnected: Boolean
        get() = mediaController != null

    fun connect() {
        disconnect()
        mediaBrowser = MediaBrowser(context, VLC_PLAYBACK_SERVICE, connectionCallback, null).also { it.connect() }
    }

    fun disconnect() {
        mediaController?.unregisterCallback(metadataCallback)
        mediaBrowser?.disconnect()
        mediaBrowser = null
        mediaController = null
    }

    fun currentPlayback(): VlcPlayback? {
        val vlcSession = mediaController ?: return null
        val playbackState = vlcSession.playbackState ?: return null
        // After the player closes the session keeps the last title and duration, with position 0
        if (playbackState.state == PlaybackState.STATE_STOPPED || playbackState.state == PlaybackState.STATE_NONE) {
            return null
        }
        val metadata = vlcSession.metadata ?: return null
        val durationMillis = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION)
        // VLC reports the new title before the duration, and a zero duration matches sub-second clips
        if (durationMillis <= 0) return null
        return VlcPlayback(
            title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE).orEmpty(),
            durationMillis = durationMillis,
            positionMillis = playbackState.position,
        )
    }
}
