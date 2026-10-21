package com.sih2026.palash.voice

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sih2026.palash.tts.LivePiperTtsEngine
import com.sih2026.palash.tts.SantaliOlChikiTransliteration
import com.sih2026.palash.ui.screens.result.TranslationResultViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.security.MessageDigest

@RunWith(AndroidJUnit4::class)
class LiveAudioHandoffProofTest {

    private lateinit var application: Application
    private lateinit var piperEngine: LivePiperTtsEngine

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        piperEngine = LivePiperTtsEngine(application)
    }

    private fun calculateSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = file.readBytes()
        val hash = digest.digest(bytes)
        return hash.joinToString("") { "%02x".format(it) }
    }

    private suspend fun synthesizeWithRetry(santaliText: String): com.sih2026.palash.tts.LiveTtsResult {
        var attempts = 0
        while (attempts < 60) {
            val res = piperEngine.synthesizeLive(santaliText)
            if (res.state != com.sih2026.palash.tts.LiveTtsState.UNAVAILABLE) {
                return res
            }
            kotlinx.coroutines.delay(500)
            attempts++
        }
        return piperEngine.synthesizeLive(santaliText)
    }

    @Test
    fun testLiveAudioHandoff_PhysicalProof_A_B_and_F() = runBlocking {
        println("==================================================")
        println("BEGINNING LIVE AUDIO HANDOFF PHYSICAL PROOF TEST")
        println("==================================================")

        // --- TEST A: "एक" ---
        val hindiA = "एक"
        val santaliOlChikiA = "ᱢᱤᱫ"
        val romanizedA = SantaliOlChikiTransliteration.toRoman(santaliOlChikiA)
        assertEquals("mid", romanizedA)

        val ttsResultA = synthesizeWithRetry(santaliOlChikiA)
        assertNotNull("Test A synthesis result must not be null", ttsResultA)
        val fileA = ttsResultA.audioFile
        assertNotNull("Test A synthesis must produce a file (state: ${ttsResultA.state}, error: ${ttsResultA.errorMessage})", fileA)
        assertTrue("Test A file must exist", fileA!!.exists())
        assertTrue("Test A file size > 0", fileA.length() > 0)
        val shaA = calculateSha256(fileA)

        println("[TEST A]")
        println("Hindi: $hindiA")
        println("Santali: $santaliOlChikiA")
        println("Romanized: $romanizedA")
        println("Generated WAV: ${fileA.absolutePath}")
        println("Size: ${fileA.length()} bytes")
        println("SHA-256: $shaA")

        // Handoff to ViewModel A
        val vmA = TranslationResultViewModel(application)
        vmA.setTranslation(
            hindi = hindiA,
            santaliNotice = santaliOlChikiA,
            liveAudioPath = fileA.absolutePath,
            originalAudioPath = null
        )

        // Trigger playback
        vmA.playAudioClicked()
        val stateA = vmA.uiState.value
        assertEquals("Audio source for Test A must be LIVE_PIPER", "LIVE_PIPER", stateA.activeAudioSource)
        assertEquals("Live audio path in state must match file A", fileA.absolutePath, stateA.liveAudioPath)
        println("Verified played file A: ${stateA.liveAudioPath} (Audio Source: ${stateA.activeAudioSource})")

        // --- TEST B: "दो" ---
        val hindiB = "दो"
        val santaliOlChikiB = "ᱵᱟᱨ"
        val romanizedB = SantaliOlChikiTransliteration.toRoman(santaliOlChikiB)
        assertEquals("bar", romanizedB)

        val ttsResultB = synthesizeWithRetry(santaliOlChikiB)
        assertNotNull("Test B synthesis result must not be null", ttsResultB)
        val fileB = ttsResultB.audioFile
        assertNotNull("Test B synthesis must produce a file (state: ${ttsResultB.state}, error: ${ttsResultB.errorMessage})", fileB)
        assertTrue("Test B file must exist", fileB!!.exists())
        assertTrue("Test B file size > 0", fileB.length() > 0)
        val shaB = calculateSha256(fileB)

        println("\n[TEST B]")
        println("Hindi: $hindiB")
        println("Santali: $santaliOlChikiB")
        println("Romanized: $romanizedB")
        println("Generated WAV: ${fileB.absolutePath}")
        println("Size: ${fileB.length()} bytes")
        println("SHA-256: $shaB")

        assertNotEquals("SHA-256 A and B must be unique", shaA, shaB)

        // Handoff to ViewModel B
        val vmB = TranslationResultViewModel(application)
        vmB.setTranslation(
            hindi = hindiB,
            santaliNotice = santaliOlChikiB,
            liveAudioPath = fileB.absolutePath,
            originalAudioPath = null
        )

        // Trigger playback
        vmB.playAudioClicked()
        val stateB = vmB.uiState.value
        assertEquals("Audio source for Test B must be LIVE_PIPER", "LIVE_PIPER", stateB.activeAudioSource)
        assertEquals("Live audio path in state must match file B", fileB.absolutePath, stateB.liveAudioPath)
        println("Verified played file B: ${stateB.liveAudioPath} (Audio Source: ${stateB.activeAudioSource})")

        // --- TEST F: NEW SENTENCE ---
        val hindiF = "आज हम कक्षा में गणित और गिनती का नया पाठ पढ़ेंगे।"
        val santaliOlChikiF = "ᱛᱮᱦᱮᱧ ᱟᱵᱚ ᱠᱞᱟᱥ ᱨᱮ ᱮᱞᱠᱷᱟ ᱟᱨ ᱞᱮᱠᱷᱟ ᱨᱮᱱᱟᱜ ᱱᱟᱣᱟ ᱯᱟᱴ ᱵᱚᱱ ᱯᱟᱲᱦᱟᱣᱟ।"
        val romanizedF = SantaliOlChikiTransliteration.toRoman(santaliOlChikiF)

        val ttsResultF = synthesizeWithRetry(santaliOlChikiF)
        assertNotNull("Test F synthesis result must not be null", ttsResultF)
        val fileF = ttsResultF.audioFile
        assertNotNull("Test F synthesis must produce a file (state: ${ttsResultF.state}, error: ${ttsResultF.errorMessage})", fileF)
        assertTrue("Test F file must exist", fileF!!.exists())
        assertTrue("Test F file size > 0", fileF.length() > 0)
        val shaF = calculateSha256(fileF)

        println("\n[TEST F - NEW SENTENCE]")
        println("Hindi: $hindiF")
        println("Santali: $santaliOlChikiF")
        println("Romanized: $romanizedF")
        println("Generated WAV: ${fileF.absolutePath}")
        println("Size: ${fileF.length()} bytes")
        println("SHA-256: $shaF")

        // Handoff to ViewModel F
        val vmF = TranslationResultViewModel(application)
        vmF.setTranslation(
            hindi = hindiF,
            santaliNotice = santaliOlChikiF,
            liveAudioPath = fileF.absolutePath,
            originalAudioPath = null
        )

        // Trigger playback
        vmF.playAudioClicked()
        val stateF = vmF.uiState.value
        assertEquals("Audio source for Test F must be LIVE_PIPER", "LIVE_PIPER", stateF.activeAudioSource)
        assertEquals("Live audio path in state must match file F", fileF.absolutePath, stateF.liveAudioPath)
        assertFalse("Must NOT play static asset for Test F", stateF.liveAudioPath?.contains("number_1.wav") == true)
        assertFalse("Must NOT play static asset for Test F", stateF.liveAudioPath?.contains("AS001") == true)
        println("Verified played file F: ${stateF.liveAudioPath} (Audio Source: ${stateF.activeAudioSource})")

        // --- TEST ORIGINAL VOICE REPLAY ---
        val dummyOriginalFile = File(application.cacheDir, "original_hindi_rec.pcm")
        dummyOriginalFile.writeBytes(ByteArray(1000))
        val vmOrig = TranslationResultViewModel(application)
        vmOrig.setTranslation(
            hindi = hindiA,
            santaliNotice = santaliOlChikiA,
            liveAudioPath = fileA.absolutePath,
            originalAudioPath = dummyOriginalFile.absolutePath
        )
        vmOrig.playOriginalAudioClicked()
        val stateOrig = vmOrig.uiState.value
        assertTrue("Playing original audio flag must be true", stateOrig.isPlayingOriginalAudio)
        assertEquals("Original audio source must be ORIGINAL_HINDI", "ORIGINAL_HINDI", stateOrig.activeAudioSource)

        println("\n==================================================")
        println("ALL LIVE AUDIO HANDOFF PROOF TESTS COMPLETED SUCCESSFULLY")
        println("==================================================")
    }
}
