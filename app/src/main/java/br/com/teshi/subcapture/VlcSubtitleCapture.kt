package br.com.teshi.subcapture

import android.content.Context
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SubtitleCaptureFailure(val userMessage: String, reason: String) : Exception(reason)

data class SubtitleCapture(val subtitle: String?, val positionMillis: Long, val fileName: String)

class VlcSubtitleCapture(private val context: Context, private val vlcConnection: VlcConnection) {
    private var cachedVideoFile: VideoFileEntry? = null
    private var cachedCues: List<SubtitleCue> = emptyList()

    suspend fun subtitleOnScreen(): SubtitleCapture {
        val playback = readPlayback()
        return withContext(Dispatchers.IO) {
            val videoFile = findVideoFile(playback)
            val subtitle = subtitleAt(cuesOf(videoFile), playback.positionMillis)
            SubtitleCapture(subtitle, playback.positionMillis, videoFile.displayName)
        }
    }

    private fun readPlayback(): VlcPlayback {
        if (!vlcConnection.isConnected) {
            vlcConnection.connect()
            throw SubtitleCaptureFailure(
                "Nenhum vídeo aberto no VLC", "not connected to VLC's PlaybackService, connecting again"
            )
        }
        return vlcConnection.currentPlayback() ?: throw SubtitleCaptureFailure(
            "Nenhum vídeo aberto no VLC", "VLC's media session has no playback state or no metadata"
        )
    }

    private fun findVideoFile(playback: VlcPlayback): VideoFileEntry {
        val candidates = context.videoFilesLasting(playback.durationMillis)
        return pickEpisodeFile(candidates, playback.title) ?: throw SubtitleCaptureFailure(
            "Arquivo do vídeo não encontrado",
            "no MediaStore video lasting ${playback.durationMillis}ms (within 1s) for \"${playback.title}\""
        )
    }

    private fun cuesOf(videoFile: VideoFileEntry): List<SubtitleCue> {
        if (videoFile == cachedVideoFile) return cachedCues
        val cues = try {
            context.readSubtitleCuesOf(videoFile)
        } catch (error: IOException) {
            throw SubtitleCaptureFailure("Vídeo sem legenda em texto", "${videoFile.displayName}: ${error.message}")
        }
        cachedVideoFile = videoFile
        cachedCues = cues
        return cues
    }
}
