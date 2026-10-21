package com.sih2026.palash.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying M3 Santali audio data models, asset paths, and playback state machine.
 */
class SantaliAudioPlaybackUnitTest {

    @Test
    fun testSantaliAudioMetadataDefaults() {
        val meta = SantaliAudioMetadata(
            audioId = "number_1",
            relativeAssetPath = "audio/santali/g2_counting/numbers/number_1.wav",
            category = "numbers",
            grade = 2
        )

        assertEquals("number_1", meta.audioId)
        assertEquals("audio/santali/g2_counting/numbers/number_1.wav", meta.relativeAssetPath)
        assertEquals("numbers", meta.category)
        assertEquals(2, meta.grade)
        assertEquals(22050, meta.sampleRate)
        assertEquals(1, meta.channels)
        assertEquals(16, meta.bitsPerSample)
        assertEquals("Piper-generated", meta.generationMethod)
        assertEquals("UNVERIFIED", meta.validationStatus)
    }

    @Test
    fun testAudioPlaybackStateEnumValues() {
        val states = AudioPlaybackState.values()
        assertEquals(5, states.size)
        assertTrue(states.contains(AudioPlaybackState.IDLE))
        assertTrue(states.contains(AudioPlaybackState.LOADING))
        assertTrue(states.contains(AudioPlaybackState.PLAYING))
        assertTrue(states.contains(AudioPlaybackState.COMPLETED))
        assertTrue(states.contains(AudioPlaybackState.ERROR))
    }

    @Test
    fun testFakeAudioPlaybackEngineLifecycle() {
        val fakeEngine = FakeAudioPlaybackEngine()
        assertEquals(AudioPlaybackState.IDLE, fakeEngine.currentState)

        var lastState: AudioPlaybackState = AudioPlaybackState.IDLE
        var lastError: String? = null

        fakeEngine.playAsset("audio/santali/g2_counting/numbers/number_1.wav") { state, error ->
            lastState = state
            lastError = error
        }

        assertEquals(AudioPlaybackState.COMPLETED, fakeEngine.currentState)
        assertEquals(AudioPlaybackState.COMPLETED, lastState)
        assertNull(lastError)

        fakeEngine.stop()
        assertEquals(AudioPlaybackState.IDLE, fakeEngine.currentState)

        fakeEngine.release()
        assertEquals(AudioPlaybackState.IDLE, fakeEngine.currentState)
    }
}

/**
 * Fake implementation of AudioPlaybackEngine for unit testing without Android MediaPlayer dependencies.
 */
class FakeAudioPlaybackEngine(
    private val shouldFail: Boolean = false
) : AudioPlaybackEngine {

    private var _currentState: AudioPlaybackState = AudioPlaybackState.IDLE
    override val currentState: AudioPlaybackState get() = _currentState
    override val currentPlayingAudioId = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)

    override fun playAsset(assetPath: String, audioId: String, onStateChanged: (AudioPlaybackState, String?) -> Unit) {
        _currentState = AudioPlaybackState.LOADING
        currentPlayingAudioId.value = audioId
        onStateChanged(AudioPlaybackState.LOADING, null)

        if (shouldFail) {
            _currentState = AudioPlaybackState.ERROR
            currentPlayingAudioId.value = null
            onStateChanged(AudioPlaybackState.ERROR, "Simulated playback error")
        } else {
            _currentState = AudioPlaybackState.PLAYING
            onStateChanged(AudioPlaybackState.PLAYING, null)
            _currentState = AudioPlaybackState.COMPLETED
            onStateChanged(AudioPlaybackState.COMPLETED, null)
        }
    }

    override fun stop() {
        _currentState = AudioPlaybackState.IDLE
        currentPlayingAudioId.value = null
    }

    override fun release() {
        stop()
    }
}
