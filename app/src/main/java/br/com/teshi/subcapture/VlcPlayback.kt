package br.com.teshi.subcapture

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.browse.MediaBrowser
import android.media.session.MediaController
import android.util.Log

private val VLC_PLAYBACK_SERVICE = ComponentName("org.videolan.vlc", "org.videolan.vlc.PlaybackService")

data class VlcPlayback(val title: String, val durationMillis: Long, val positionMillis: Long)

class VlcConnection(private val context: Context) {
    private var mediaBrowser: MediaBrowser? = null
    private var mediaController: MediaController? = null

    private val connectionCallback = object : MediaBrowser.ConnectionCallback() {
        override fun onConnected() {
            mediaController = mediaBrowser?.let { MediaController(context, it.sessionToken) }
            if (context.isDebuggableBuild) Log.d(CAPTURE_LOG_TAG, "connected to $VLC_PLAYBACK_SERVICE")
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
        mediaBrowser?.disconnect()
        mediaBrowser = null
        mediaController = null
    }

    fun currentPlayback(): VlcPlayback? {
        val vlcSession = mediaController ?: return null
        val positionMillis = vlcSession.playbackState?.position ?: return null
        val metadata = vlcSession.metadata ?: return null
        return VlcPlayback(
            title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE).orEmpty(),
            durationMillis = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION),
            positionMillis = positionMillis,
        )
    }
}
