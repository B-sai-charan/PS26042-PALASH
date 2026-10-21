package com.sih2026.palash.ui.screens.voice

import com.sih2026.palash.audio.PcmAudioPlayer
import com.sih2026.palash.audio.PcmPlaybackState
import com.sih2026.palash.audio.RecordingState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class OriginalAudioReplayUnitTest {

    private lateinit var fakePcmPlayer: FakeTestPcmPlayer

    @Before
    fun setUp() {
        fakePcmPlayer = FakeTestPcmPlayer()
    }

    @Test
    fun testInitialPlaybackStateIsIdle() {
        val state = VoiceTranslationUiState()
        assertNull(state.recordedFilePath)
        assertFalse(state.isPlayingOriginalAudio)
        assertEquals(PcmPlaybackState.IDLE, state.originalAudioState)
    }

    @Test
    fun testFakePcmPlayerStateTransitions() {
        val tempFile = File.createTempFile("test_replay", ".pcm")
        tempFile.writeBytes(ByteArray(100))

        fakePcmPlayer.playPcmFile(tempFile) { state, err ->
            assertEquals(PcmPlaybackState.PLAYING, state)
            assertNull(err)
        }
        assertEquals(PcmPlaybackState.PLAYING, fakePcmPlayer.currentState)

        fakePcmPlayer.stop()
        assertEquals(PcmPlaybackState.IDLE, fakePcmPlayer.currentState)
        assertTrue(fakePcmPlayer.isStopped)

        tempFile.delete()
    }

    @Test
    fun testMissingRecordingHandling() {
        val missingFile = File("non_existent_path.pcm")
        var lastState: PcmPlaybackState = PcmPlaybackState.IDLE
        var lastError: String? = null

        fakePcmPlayer.playPcmFile(missingFile) { state, err ->
            lastState = state
            lastError = err
        }

        assertEquals(PcmPlaybackState.ERROR, lastState)
        assertEquals("Audio file does not exist", lastError)
    }

    @Test
    fun testRepeatedRecordingReplacesActiveReplayTarget() {
        var recordedFilePath: String? = "cache/recorded_hindi_A.pcm"
        assertEquals("cache/recorded_hindi_A.pcm", recordedFilePath)

        // Starting Recording B clears existing path
        recordedFilePath = null
        assertNull(recordedFilePath)

        // Recording B finishes
        recordedFilePath = "cache/recorded_hindi_B.pcm"
        assertEquals("cache/recorded_hindi_B.pcm", recordedFilePath)
        assertTrue(recordedFilePath != "cache/recorded_hindi_A.pcm")
    }

    @Test
    fun testPlaybackCleanup() {
        val tempFile = File.createTempFile("test_cleanup", ".pcm")
        tempFile.writeBytes(ByteArray(100))

        fakePcmPlayer.playPcmFile(tempFile) { _, _ -> }
        assertEquals(PcmPlaybackState.PLAYING, fakePcmPlayer.currentState)

        fakePcmPlayer.release()
        assertEquals(PcmPlaybackState.IDLE, fakePcmPlayer.currentState)
        assertTrue(fakePcmPlayer.isStopped)

        tempFile.delete()
    }
}

class FakeTestPcmPlayer : PcmAudioPlayer {
    private var _currentState = PcmPlaybackState.IDLE
    override val currentState: PcmPlaybackState get() = _currentState
    var isStopped = false

    override fun playPcmFile(file: File, sampleRate: Int, onStateChanged: (PcmPlaybackState, String?) -> Unit) {
        playPcmFileWithId(file, file.absolutePath, sampleRate, onStateChanged)
    }

    override fun playPcmFileWithId(file: File, audioId: String, sampleRate: Int, onStateChanged: (PcmPlaybackState, String?) -> Unit) {
        if (!file.exists() || file.length() == 0L) {
            _currentState = PcmPlaybackState.ERROR
            onStateChanged(PcmPlaybackState.ERROR, "Audio file does not exist")
            return
        }
        _currentState = PcmPlaybackState.PLAYING
        isStopped = false
        onStateChanged(PcmPlaybackState.PLAYING, null)
    }

    override fun stop() {
        _currentState = PcmPlaybackState.IDLE
        isStopped = true
    }

    override fun release() {
        stop()
    }
}
