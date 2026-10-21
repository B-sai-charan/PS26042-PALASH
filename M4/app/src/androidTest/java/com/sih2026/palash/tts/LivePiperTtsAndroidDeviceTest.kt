package com.sih2026.palash.tts

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sih2026.palash.audio.SantaliAudioRepositoryImpl
import com.sih2026.palash.data.local.db.DatabaseProvider
import com.sih2026.palash.data.local.entity.TranslationCacheEntity
import com.sih2026.palash.data.repository.TranslationRepositoryImpl
import com.sih2026.palash.translation.ValidationStatus
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LivePiperTtsAndroidDeviceTest {

    private lateinit var context: Context
    private lateinit var audioRepo: SantaliAudioRepositoryImpl

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        audioRepo = SantaliAudioRepositoryImpl(context)
    }

    @Test
    fun testLivePiperTts_SmokeTest_And_PipelineA_And_PipelineB_And_Fallback() = runBlocking {
        val piperEngine = LivePiperTtsEngine(context)
        piperEngine.awaitReady(30000L)

        // --- SMOKE TEST ---
        val smokeInput = "ᱢᱤᱫ"
        val smokeStart = System.currentTimeMillis()
        val smokeResult = piperEngine.synthesizeLive(smokeInput)
        val smokeTotalTime = System.currentTimeMillis() - smokeStart

        assertEquals("Smoke Test: State must be READY", LiveTtsState.READY, smokeResult.state)
        assertNotNull("Smoke Test: Audio file must be generated", smokeResult.audioFile)
        assertTrue("Smoke Test: Generated audio file must exist on disk", smokeResult.audioFile!!.exists())
        assertTrue("Smoke Test: Generated audio file must be > 0 bytes", smokeResult.audioFile!!.length() > 0)
        assertEquals("Smoke Test: Romanized text must match 'mid'", "mid", smokeResult.romanizedText)

        println("=== PIPER SMOKE TEST PASSED ===")
        println("Input: $smokeInput, Roman: ${smokeResult.romanizedText}")
        println("ModelLoad: ${smokeResult.modelLoadTimeMs}ms, Phonemize: ${smokeResult.phonemizeTimeMs}ms, Inference: ${smokeResult.inferenceTimeMs}ms, WAVGen: ${smokeResult.wavGenTimeMs}ms, Total: ${smokeResult.totalTtsTimeMs}ms")
        println("Generated WAV: ${smokeResult.audioFile!!.name} (${smokeResult.audioFile!!.length()} bytes)")

        // --- PIPELINE TEST A ---
        val inputA = "एक"
        val santaliA = "ᱢᱤᱫ"
        val resultA = piperEngine.synthesizeLive(santaliA)

        assertEquals("TEST A: Piper synthesis must succeed", LiveTtsState.READY, resultA.state)
        assertNotNull("TEST A: Audio file A generated", resultA.audioFile)
        val fileA = resultA.audioFile!!

        // --- PIPELINE TEST B ---
        val inputB = "दो"
        val santaliB = "ᱵᱟᱨ"
        val resultB = piperEngine.synthesizeLive(santaliB)

        assertEquals("TEST B: Piper synthesis must succeed", LiveTtsState.READY, resultB.state)
        assertNotNull("TEST B: Audio file B generated", resultB.audioFile)
        val fileB = resultB.audioFile!!

        // --- A/B ISOLATION VERIFICATION ---
        assertNotEquals("Input A and B must produce different generated audio files", fileA.absolutePath, fileB.absolutePath)
        assertNotEquals("Input A and B must produce different Romanized text", resultA.romanizedText, resultB.romanizedText)

        // --- FALLBACK VERIFICATION ---
        val fallbackAudioId = "number_1"
        assertTrue("Fallback check: Pre-recorded M3 WAV asset number_1 must exist", audioRepo.hasAudioAsset(fallbackAudioId))
        val fallbackPath = audioRepo.getAssetPathForAudio(fallbackAudioId)
        assertNotNull("Fallback check: Asset path for number_1 must not be null", fallbackPath)

        piperEngine.release()

        println("=== PIPER LIVE PIPELINE TEST PASSED ===")
        println("A: input=$inputA, santali=$santaliA, roman=${resultA.romanizedText}, infTime=${resultA.inferenceTimeMs}ms, file=${fileA.name}")
        println("B: input=$inputB, santali=$santaliB, roman=${resultB.romanizedText}, infTime=${resultB.inferenceTimeMs}ms, file=${fileB.name}")
        println("Fallback: M3 pre-recorded WAV path=$fallbackPath")
    }
}
