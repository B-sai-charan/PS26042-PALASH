package com.sih2026.palash.data.network

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import com.sih2026.palash.classroom.ClassroomMessageType
import com.sih2026.palash.classroom.ClassroomNetworkUtils
import com.sih2026.palash.classroom.ClassroomTranslationMessage
import com.sih2026.palash.classroom.TeacherClassroomServer
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Physical instrumented smoke test executing TeacherClassroomServer on Lenovo Tab K10 hardware.
 */
@RunWith(AndroidJUnit4::class)
class PhysicalClassroomServerSmokeTest {

    @Test
    fun testPhysicalLenovo_teacherClassroomServerSmokeTest(): Unit {
        val server = TeacherClassroomServer(port = 8080)
        val started = server.start()
        assertTrue("TeacherClassroomServer must start on physical device", started)

        val localIp = ClassroomNetworkUtils.getLocalIPv4Address()
        Log.i("PHYSICAL_SERVER_TEST", "Lenovo local IPv4 resolved: $localIp")

        // Test GET /health
        val healthUrl = URL("http://127.0.0.1:8080/health")
        val healthConn = healthUrl.openConnection() as HttpURLConnection
        healthConn.connectTimeout = 3000
        healthConn.readTimeout = 3000
        assertEquals(200, healthConn.responseCode)
        val healthBody = BufferedReader(InputStreamReader(healthConn.inputStream)).readText()
        assertTrue(healthBody.contains("\"status\":\"ok\""))
        healthConn.disconnect()

        // Test GET /classroom/latest before publish (404)
        val emptyUrl = URL("http://127.0.0.1:8080/classroom/latest")
        val emptyConn = emptyUrl.openConnection() as HttpURLConnection
        emptyConn.connectTimeout = 3000
        emptyConn.readTimeout = 3000
        assertEquals(404, emptyConn.responseCode)
        emptyConn.disconnect()

        // Publish test message
        val msg = ClassroomTranslationMessage(
            messageId = "smoke_msg_001",
            type = ClassroomMessageType.CLASSROOM_TRANSLATION,
            grade = 2,
            section = "A",
            subject = "mathematics",
            lessonId = 1,
            timestamp = System.currentTimeMillis(),
            hindiText = "TEST_HINDI_PHYSICAL",
            santaliText = "TEST_SANTALI_PHYSICAL",
            audioId = "number_1"
        )
        val published = server.publishMessage(msg)
        assertTrue(published)

        // Test GET /classroom/latest after publish (200)
        val latestUrl = URL("http://127.0.0.1:8080/classroom/latest")
        val latestConn = latestUrl.openConnection() as HttpURLConnection
        latestConn.connectTimeout = 3000
        latestConn.readTimeout = 3000
        assertEquals(200, latestConn.responseCode)
        val latestBody = BufferedReader(InputStreamReader(latestConn.inputStream)).readText()
        val deserialized = ClassroomTranslationMessage.fromJson(latestBody, Gson())
        assertNotNull(deserialized)
        assertEquals("smoke_msg_001", deserialized?.messageId)
        assertEquals("TEST_HINDI_PHYSICAL", deserialized?.hindiText)
        latestConn.disconnect()

        server.stop()
        Log.i("PHYSICAL_SERVER_TEST", "TeacherClassroomServer smoke test passed successfully on physical device.")
    }
}
