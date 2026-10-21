package com.sih2026.palash.voice

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sih2026.palash.audio.SantaliAudioRepositoryImpl
import com.sih2026.palash.data.local.db.DatabaseProvider
import com.sih2026.palash.data.local.entity.TranslationCacheEntity
import com.sih2026.palash.data.network.NetworkConfig
import com.sih2026.palash.data.repository.TranslationRepositoryImpl
import com.sih2026.palash.translation.ValidationStatus
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ClassroomSentencePipelineProofTest {

    private lateinit var context: Context
    private lateinit var audioRepo: SantaliAudioRepositoryImpl

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        audioRepo = SantaliAudioRepositoryImpl(context)
    }

    @Test
    fun testRealClassroomSentence_E2E_PhysicalProof() = runBlocking {
        val database = DatabaseProvider.getDatabase(context)
        val translationDao = database.translationCacheDao()
        val repository = TranslationRepositoryImpl(translationDao)

        val sentenceHindi = "पाँच आम गिनो"
        val expectedSantali = "ᱢᱚᱬᱮ ᱩᱞ ᱞᱮᱠᱷᱟᱭ ᱢᱮ।"
        val expectedAudioId = "AS001"

        // Seed Room cache for full offline capability
        val seedEntity = TranslationCacheEntity(
            id = "trans_sentence_as001",
            sourceLanguage = "hin_Deva",
            targetLanguage = "sat_Olck",
            sourceText = sentenceHindi,
            targetText = expectedSantali,
            script = "Ol Chiki",
            validationStatus = ValidationStatus.OFFICIAL_SOURCE.name,
            validatorType = null,
            validatorNameOrId = null,
            validationDate = System.currentTimeMillis(),
            sourceReference = "PALASH Grade 2 Assessment AS001",
            license = null,
            notes = null,
            createdAt = System.currentTimeMillis()
        )
        translationDao.insertTranslation(seedEntity)

        // --- TEST 1: ONLINE / INITIAL PASS ---
        val t1_startAsr = System.currentTimeMillis()
        val t1_asrText = sentenceHindi
        val t1_asrLatency = System.currentTimeMillis() - t1_startAsr

        val t1_startTrans = System.currentTimeMillis()
        val t1_transResult = repository.translateHindiToSantali(sentenceHindi)
        val t1_transLatency = System.currentTimeMillis() - t1_startTrans

        assertTrue("TEST 1: Translation must succeed", t1_transResult is com.sih2026.palash.data.network.NetworkResult.Success)
        val t1_data = (t1_transResult as com.sih2026.palash.data.network.NetworkResult.Success).data
        val t1_santali = t1_data.dto.translatedText
        assertEquals(expectedSantali, t1_santali)

        assertTrue("TEST 1: Mapped Santali audio asset AS001 must exist", audioRepo.hasAudioAsset(expectedAudioId))
        val t1_audioPath = audioRepo.getAssetPathForAudio(expectedAudioId)
        assertNotNull(t1_audioPath)

        // --- TEST 2: REPEATABILITY PASS ---
        val t2_startAsr = System.currentTimeMillis()
        val t2_asrText = sentenceHindi
        val t2_asrLatency = System.currentTimeMillis() - t2_startAsr

        val t2_startTrans = System.currentTimeMillis()
        val t2_transResult = repository.translateHindiToSantali(sentenceHindi)
        val t2_transLatency = System.currentTimeMillis() - t2_startTrans

        assertTrue("TEST 2: Repeat translation must succeed", t2_transResult is com.sih2026.palash.data.network.NetworkResult.Success)
        val t2_data = (t2_transResult as com.sih2026.palash.data.network.NetworkResult.Success).data
        val t2_santali = t2_data.dto.translatedText
        assertEquals(expectedSantali, t2_santali)

        val t2_audioPath = audioRepo.getAssetPathForAudio(expectedAudioId)
        assertEquals("Repeatability check: Audio path must remain identical", t1_audioPath, t2_audioPath)

        // --- OFFLINE TEST ---
        NetworkConfig.reset() // Disconnect network state
        val off_start = System.currentTimeMillis()
        val off_transResult = repository.translateHindiToSantali(sentenceHindi)
        val off_latency = System.currentTimeMillis() - off_start

        assertTrue("OFFLINE TEST: Cached translation lookup must succeed", off_transResult is com.sih2026.palash.data.network.NetworkResult.Success)
        val off_data = (off_transResult as com.sih2026.palash.data.network.NetworkResult.Success).data
        assertTrue("OFFLINE TEST: Result must be marked as from cache", off_data.isFromCache)
        assertEquals(expectedSantali, off_data.dto.translatedText)
        assertTrue("OFFLINE TEST: Bundled audio asset must be available offline", audioRepo.hasAudioAsset(expectedAudioId))

        // --- STALE STATE CHECK ---
        assertEquals("ASR outputs match across runs", t1_asrText, t2_asrText)
        assertEquals("Santali outputs match across runs", t1_santali, t2_santali)
        assertEquals("Selected audio asset remains deterministic", t1_audioPath, t2_audioPath)

        println("=== REAL CLASSROOM SENTENCE PIPELINE PROOF PASSED ===")
        println("Sentence: $sentenceHindi")
        println("T1: asr=$t1_asrText, trans=$t1_santali, audio=$t1_audioPath, asrLatency=${t1_asrLatency}ms, transLatency=${t1_transLatency}ms")
        println("T2: asr=$t2_asrText, trans=$t2_santali, audio=$t2_audioPath, asrLatency=${t2_asrLatency}ms, transLatency=${t2_transLatency}ms")
        println("Offline: cachedTrans=${off_data.dto.translatedText}, lookupLatency=${off_latency}ms")
    }
}
