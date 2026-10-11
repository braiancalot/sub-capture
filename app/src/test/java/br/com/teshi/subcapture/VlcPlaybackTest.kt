package br.com.teshi.subcapture

import android.media.session.PlaybackState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VlcPlaybackTest {
    @Test
    fun `a playing or paused session with a duration has a video open`() {
        assertTrue(isVideoOpen(PlaybackState.STATE_PLAYING, durationMillis = 1294544))
        assertTrue(isVideoOpen(PlaybackState.STATE_PAUSED, durationMillis = 1294544))
    }

    @Test
    fun `a stopped session has no video open even though it keeps the last duration`() {
        assertFalse(isVideoOpen(PlaybackState.STATE_STOPPED, durationMillis = 1294544))
        assertFalse(isVideoOpen(PlaybackState.STATE_NONE, durationMillis = 1294544))
    }

    @Test
    fun `a session that has not reported a duration yet has no video open`() {
        assertFalse(isVideoOpen(PlaybackState.STATE_PLAYING, durationMillis = 0))
    }
}
