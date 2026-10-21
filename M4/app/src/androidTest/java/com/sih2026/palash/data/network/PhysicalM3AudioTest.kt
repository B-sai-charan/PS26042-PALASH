package com.sih2026.palash.data.network

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sih2026.palash.audio.AndroidAudioPlaybackEngineImpl
import com.sih2026.palash.audio.AudioPlaybackState
import com.sih2026.palash.audio.SantaliAudioRepositoryImpl
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented physical-device test verifying M3 Grade-2 Santali audio asset playback on physical iQOO Neo9 Pro.
 * Test asset: audio/santali/g2_counting/numbers/number_1.wav (22050 Hz, mono, 16-bit PCM).
 */
@RunWith(AndroidJUnit4::class)
class PhysicalM3AudioTest {

    @Test
    fun testPhysicalAndroid_grade2AudioAssetExists() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repo = SantaliAudioRepositoryImpl(context)

        val assetPath = repo.getAssetPathForAudio("number_1")
        assertNotNull("Expected asset path for number_1 to be resolved", assetPath)
        assertEquals("audio/santali/g2_counting/numbers/number_1.wav", assetPath)

        val afd = context.assets.openFd(assetPath!!)
        assertNotNull("Expected AssetFileDescriptor for number_1.wav to open successfully", afd)
        assertTrue("WAV asset file length must be > 0 bytes", afd.length > 0)
        Log.i("PHYSICAL_M3_AUDIO", "Successfully verified WAV asset: $assetPath (${afd.length} bytes)")
        afd.close()
    }

    @Test
    fun testPhysicalAndroid_playGrade2Number1Wav(): Unit = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repo = SantaliAudioRepositoryImpl(context)
        val player = AndroidAudioPlaybackEngineImpl(context)

        val assetPath = repo.getAssetPathForAudio("number_1")
        assertNotNull("Asset path for number_1 must exist", assetPath)

        val completionDeferred = CompletableDeferred<Boolean>()
        var playedSuccessfully = false
        var playbackError: String? = null

        Log.i("PHYSICAL_M3_AUDIO", "Starting physical playback test for asset: $assetPath")

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            player.playAsset(assetPath!!) { state, error ->
                Log.i("PHYSICAL_M3_AUDIO", "Playback state changed: state=$state, error=$error")
                when (state) {
                    AudioPlaybackState.PLAYING -> {
                        Log.i("PHYSICAL_M3_AUDIO", "Decoder accepted WAV and playback started successfully.")
                    }
                    AudioPlaybackState.COMPLETED -> {
                        Log.i("PHYSICAL_M3_AUDIO", "Playback completed successfully.")
                        playedSuccessfully = true
                        if (!completionDeferred.isCompleted) completionDeferred.complete(true)
                    }
                    AudioPlaybackState.ERROR -> {
                        Log.e("PHYSICAL_M3_AUDIO", "Playback failed with error: $error")
                        playbackError = error
                        if (!completionDeferred.isCompleted) completionDeferred.complete(false)
                    }
                    else -> {}
                }
            }
        }

        val result = withTimeout(5000) {
            completionDeferred.await()
        }

        assertTrue("Audio playback of number_1.wav should complete successfully on physical device: error=$playbackError", result)
        assertTrue("playedSuccessfully flag must be true", playedSuccessfully)

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            player.release()
        }
        assertEquals(AudioPlaybackState.IDLE, player.currentState)
        Log.i("PHYSICAL_M3_AUDIO", "Playback engine released successfully without resource leaks.")
    }
}
