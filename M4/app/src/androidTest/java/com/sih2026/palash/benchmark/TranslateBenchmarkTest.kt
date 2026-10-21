package com.sih2026.palash.benchmark

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sih2026.palash.data.network.NetworkClient
import com.sih2026.palash.data.network.NetworkConfig
import com.sih2026.palash.data.network.dto.TranslateRequestDto
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

data class RequestMetric(
    val phraseIndex: Int,
    val phraseText: String,
    val requestType: String, // "COLD" or "WARM_1", "WARM_2", etc.
    val startTimestampMs: Long,
    val endTimestampMs: Long,
    val roundTripMs: Long,
    val backendLatencyMs: Double,
    val overheadMs: Double,
    val translatedText: String,
    val isSuccess: Boolean
)

@RunWith(AndroidJUnit4::class)
class TranslateBenchmarkTest {

    companion object {
        private const val TAG = "M1_BENCHMARK"
    }

    @Before
    fun setUp() {
        NetworkConfig.setDevelopmentBaseUrl("http://127.0.0.1:8000")
    }

    @After
    fun tearDown() {
        NetworkConfig.reset()
    }

    @Test
    @org.junit.Ignore("Explicit benchmark runner test")
    fun runTranslationBenchmark() = runBlocking {
        val testPhrases = listOf(
            "किसानों को सरकारी योजना का लाभ मिलेगा।",
            "1 से 20 तक गिनती सीखें।",
            "पाँच आम गिनो।",
            "अक्षर पहचानो।",
            "गिनती करके सही संख्या चुनो।"
        )

        val apiService = NetworkClient.getApiService()
        assertNotNull("ApiService must be available", apiService)

        val metricsList = mutableListOf<RequestMetric>()

        Log.i(TAG, "==================================================")
        Log.i(TAG, "STARTING REAL M1 TRANSLATE BENCHMARK ON PHYSICAL DEVICE")
        Log.i(TAG, "Device: iQOO Neo9 Pro (10BE830QG8000MX)")
        Log.i(TAG, "Endpoint: http://127.0.0.1:8000/translate")
        Log.i(TAG, "==================================================")

        for ((index, phrase) in testPhrases.withIndex()) {
            val phraseNum = index + 1
            Log.i(TAG, "--- PHRASE $phraseNum: \"$phrase\" ---")

            // Cold Request (1st request for this phrase)
            val coldMetric = executeSingleRequest(apiService!!, phraseNum, phrase, "COLD")
            metricsList.add(coldMetric)

            // Warm Requests (5 repeated requests)
            for (warmIteration in 1..5) {
                kotlinx.coroutines.delay(200)
                val warmMetric = executeSingleRequest(apiService, phraseNum, phrase, "WARM_$warmIteration")
                metricsList.add(warmMetric)
            }
        }

        Log.i(TAG, "==================================================")
        Log.i(TAG, "BENCHMARK COMPLETED: ${metricsList.size} TOTAL REQUESTS EXECUTED")
        Log.i(TAG, "==================================================")

        // Print raw tabular metrics to logcat & stdout
        for (m in metricsList) {
            val line = String.format(
                "P%d [%s] | RoundTrip: %d ms | Backend: %.2f ms | Overhead: %.2f ms | Status: %s | Text: %s",
                m.phraseIndex,
                m.requestType,
                m.roundTripMs,
                m.backendLatencyMs,
                m.overheadMs,
                if (m.isSuccess) "OK" else "FAIL",
                m.translatedText
            )
            Log.i(TAG, line)
            System.out.println("BENCHMARK_LINE: $line")
        }

        // Verify all 30 requests succeeded
        val failedRequests = metricsList.filter { !it.isSuccess }
        assertEquals("All requests in benchmark should succeed, but failed: ${failedRequests.size}", 0, failedRequests.size)
    }

    private suspend fun executeSingleRequest(
        apiService: com.sih2026.palash.data.network.PalashApiService,
        phraseIndex: Int,
        phrase: String,
        requestType: String
    ): RequestMetric {
        val requestDto = TranslateRequestDto(
            text = phrase,
            sourceLanguage = "hin_Deva",
            targetLanguage = "sat_Olck"
        )

        val startTime = System.currentTimeMillis()
        val response = apiService.translate(requestDto)
        val endTime = System.currentTimeMillis()

        val roundTripMs = endTime - startTime

        if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!
            val backendLat = body.latencyMs
            val overhead = roundTripMs - backendLat

            val logMsg = "P$phraseIndex [$requestType]: RoundTrip=${roundTripMs}ms, Backend=${backendLat}ms, Overhead=${String.format("%.2f", overhead)}ms, Output=\"${body.translatedText}\""
            Log.i(TAG, logMsg)
            System.out.println("BENCHMARK_REQ: $logMsg")

            return RequestMetric(
                phraseIndex = phraseIndex,
                phraseText = phrase,
                requestType = requestType,
                startTimestampMs = startTime,
                endTimestampMs = endTime,
                roundTripMs = roundTripMs,
                backendLatencyMs = backendLat,
                overheadMs = overhead,
                translatedText = body.translatedText,
                isSuccess = true
            )
        } else {
            Log.e(TAG, "Request P$phraseIndex [$requestType] FAILED: Code=${response.code()}, Message=${response.message()}")
            return RequestMetric(
                phraseIndex = phraseIndex,
                phraseText = phrase,
                requestType = requestType,
                startTimestampMs = startTime,
                endTimestampMs = endTime,
                roundTripMs = roundTripMs,
                backendLatencyMs = -1.0,
                overheadMs = -1.0,
                translatedText = "",
                isSuccess = false
            )
        }
    }
}
