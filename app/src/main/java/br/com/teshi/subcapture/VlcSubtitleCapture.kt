package br.com.teshi.subcapture

import android.content.Context
import android.util.Log
import androidx.annotation.StringRes
import java.io.IOException
import kotlin.time.measureTimedValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class SubtitleCaptureFailure(@StringRes val userMessageId: Int, reason: String) : Exception(reason)

data class SubtitleCapture(val subtitle: String?, val positionMillis: Long, val fileName: String)

class VlcSubtitleCapture(private val context: Context, private val vlcConnection: VlcConnection) {
    private var cachedVideoFile: VideoFileEntry? = null
    private var cachedCues: List<SubtitleCue> = emptyList()

    // A tap during the preload MUST wait for it instead of reading the same file a second time
    private val cuesMutex = Mutex()

    suspend fun subtitleOnScreen(): SubtitleCapture {
        val playback = readPlayback()
        return withContext(Dispatchers.IO) {
            val videoFile = findVideoFile(playback)
            val cues = cuesMutex.withLock { cuesOf(videoFile) }
            val subtitle = subtitleAt(cues, playback.positionMillis)
            SubtitleCapture(subtitle, playback.positionMillis, videoFile.displayName)
        }
    }

    suspend fun preloadCues() {
        val playback = vlcConnection.currentPlayback() ?: return
        withContext(Dispatchers.IO) {
            val videoFile = findVideoFile(playback)
            cuesMutex.withLock { cuesOf(videoFile) }
        }
    }

    private fun readPlayback(): VlcPlayback {
        if (!vlcConnection.isConnected) {
            vlcConnection.connect()
            throw SubtitleCaptureFailure(
                R.string.capture_no_video_in_vlc, "not connected to VLC's PlaybackService, connecting again"
            )
        }
        return vlcConnection.currentPlayback() ?: throw SubtitleCaptureFailure(
            R.string.capture_no_video_in_vlc,
            "VLC's media session is stopped, or has no playback state, no metadata or no duration yet"
        )
    }

    private fun findVideoFile(playback: VlcPlayback): VideoFileEntry {
        val candidates = context.videoFilesLasting(playback.durationMillis)
        return pickEpisodeFile(candidates, playback.title) ?: throw SubtitleCaptureFailure(
            R.string.capture_video_file_not_found,
            "no MediaStore video lasting ${playback.durationMillis}ms (within 1s) for \"${playback.title}\""
        )
    }

    private fun cuesOf(videoFile: VideoFileEntry): List<SubtitleCue> {
        if (videoFile == cachedVideoFile) return cachedCues
        val (cues, readingTime) = try {
            measureTimedValue { capturableCuesOf(context.readSubtitleCuesOf(videoFile)) }
        } catch (error: IOException) {
            throw SubtitleCaptureFailure(
                R.string.capture_no_text_subtitles, "${videoFile.displayName}: ${error.message}"
            )
        }
        if (context.isDebuggableBuild) {
            Log.d(CAPTURE_LOG_TAG, "read ${cues.size} cues of ${videoFile.displayName} in $readingTime")
        }
        cachedVideoFile = videoFile
        cachedCues = cues
        return cues
    }
}

private fun capturableCuesOf(trackCues: List<SubtitleCue>): List<SubtitleCue> =
    sentenceCasedWhenTrackIsAllCaps(trackCues).map { it.copy(text = withoutClosingPeriodOrComma(it.text)) }
