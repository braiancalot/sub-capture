package br.com.teshi.subcapture

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaSessionManager
import android.service.notification.NotificationListenerService

private const val VLC_PACKAGE = "org.videolan.vlc"

// Exists only because Android lists other apps' media sessions solely for an enabled listener
class VlcSessionListener : NotificationListenerService()

data class VlcPlayback(val title: String, val durationMillis: Long, val positionMillis: Long)

private val Context.vlcSessionListener: ComponentName
    get() = ComponentName(this, VlcSessionListener::class.java)

fun Context.hasVlcSessionAccess(): Boolean =
    getSystemService(NotificationManager::class.java).isNotificationListenerAccessGranted(vlcSessionListener)

fun Context.currentVlcPlayback(): VlcPlayback? {
    val vlcSession = getSystemService(MediaSessionManager::class.java)
        .getActiveSessions(vlcSessionListener)
        .firstOrNull { it.packageName == VLC_PACKAGE }
        ?: return null
    val positionMillis = vlcSession.playbackState?.position ?: return null
    val metadata = vlcSession.metadata ?: return null
    return VlcPlayback(
        title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE).orEmpty(),
        durationMillis = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION),
        positionMillis = positionMillis,
    )
}
