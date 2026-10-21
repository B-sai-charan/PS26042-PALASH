package com.sih2026.palash.classroom

import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.ServerSocket
import java.net.URL
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying Phase 3 Step 2 TeacherClassroomServer local HTTP server,
 * lifecycle management, endpoints, message publishing, and network utilities.
 */
class TeacherClassroomServerUnitTest {

    private var activeServer: TeacherClassroomServer? = null
    private val gson = Gson()

    @After
    fun tearDown() {
        activeServer?.stop()
        activeServer = null
    }

    private fun createValidMessage(
        messageId: String = "test_message_001",
        grade: Int = 2,
        section: String = "A",
        subject: String = "mathematics",
        hindiText: String = "TEST_HINDI",
        santaliText: String = "TEST_SANTALI",
        audioId: String? = "TEST_AUDIO_1"
    ): ClassroomTranslationMessage {
        return ClassroomTranslationMessage(
            messageId = messageId,
            type = ClassroomMessageType.CLASSROOM_TRANSLATION,
            grade = grade,
            section = section,
            subject = subject,
            lessonId = 1,
            timestamp = 1725624000000L,
            hindiText = hindiText,
            santaliText = santaliText,
            audioId = audioId
        )
    }

    // 1. Valid server message publication
    @Test
    fun test01_validServerMessagePublication_returnsTrue() {
        val server = TeacherClassroomServer(port = 8881)
        activeServer = server
        val msg = createValidMessage()

        val published = server.publishMessage(msg)
        assertTrue("Valid message must be published successfully", published)
        assertEquals(msg, server.getLatestMessage())
    }

    // 2. Invalid message rejected
    @Test
    fun test02_invalidMessage_rejected() {
        val server = TeacherClassroomServer(port = 8882)
        activeServer = server
        val invalidMsg = createValidMessage(messageId = "")

        val published = server.publishMessage(invalidMsg)
        assertFalse("Invalid message must be rejected by server validator", published)
        assertNull("Server latest message must remain null after invalid publish", server.getLatestMessage())
    }

    // 3. Latest message replacement
    @Test
    fun test03_latestMessageReplacement_replacesAtomically() {
        val server = TeacherClassroomServer(port = 8883)
        activeServer = server
        val msg1 = createValidMessage(messageId = "test_msg_1", hindiText = "TEST_HINDI_1")
        val msg2 = createValidMessage(messageId = "test_msg_2", hindiText = "TEST_HINDI_2")

        server.publishMessage(msg1)
        assertEquals("test_msg_1", server.getLatestMessage()?.messageId)

        server.publishMessage(msg2)
        assertEquals("test_msg_2", server.getLatestMessage()?.messageId)
        assertEquals("TEST_HINDI_2", server.getLatestMessage()?.hindiText)
    }

    // 4. No-message state
    @Test
    fun test04_noMessageState_returnsNull() {
        val server = TeacherClassroomServer(port = 8884)
        activeServer = server
        assertNull("Unpublished server must return null for latest message", server.getLatestMessage())
    }

    // 5. Message retrieval
    @Test
    fun test05_messageRetrieval_returnsPublishedMessage() {
        val server = TeacherClassroomServer(port = 8885)
        activeServer = server
        val msg = createValidMessage(audioId = "TEST_AUDIO_RETRIEVE")
        server.publishMessage(msg)

        val retrieved = server.getLatestMessage()
        assertNotNull(retrieved)
        assertEquals("TEST_AUDIO_RETRIEVE", retrieved?.audioId)
    }

    // 6. Thread-safe publish/retrieve
    @Test
    fun test06_threadSafePublishRetrieve_operatesCorrectly() {
        val server = TeacherClassroomServer(port = 8886)
        activeServer = server
        val latch = CountDownLatch(10)

        for (i in 1..10) {
            Thread {
                val msg = createValidMessage(messageId = "test_msg_$i", hindiText = "TEST_HINDI_$i")
                server.publishMessage(msg)
                latch.countDown()
            }.start()
        }

        assertTrue("All publisher threads must complete within timeout", latch.await(3, TimeUnit.SECONDS))
        assertNotNull("Latest message must exist after multithreaded publish", server.getLatestMessage())
    }

    // 7. Server start
    @Test
    fun test07_serverStart_bindsAndStartsSuccessfully() {
        val server = TeacherClassroomServer(port = 8887)
        activeServer = server
        val started = server.start()

        assertTrue("Server must start successfully on open port", started)
        assertTrue("isServerRunning must be true after start", server.isServerRunning)
    }

    // 8. Server stop
    @Test
    fun test08_serverStop_stopsCleanly() {
        val server = TeacherClassroomServer(port = 8888)
        activeServer = server
        server.start()
        assertTrue(server.isServerRunning)

        server.stop()
        assertFalse("isServerRunning must be false after stop", server.isServerRunning)
    }

    // 9. Start called twice does not create multiple servers
    @Test
    fun test09_startCalledTwice_isIdempotent() {
        val server = TeacherClassroomServer(port = 8889)
        activeServer = server

        val start1 = server.start()
        val start2 = server.start()

        assertTrue("First start must return true", start1)
        assertTrue("Second start must return true idempotently", start2)
        assertTrue(server.isServerRunning)
    }

    // 10. Stop called twice is safe
    @Test
    fun test10_stopCalledTwice_isSafe() {
        val server = TeacherClassroomServer(port = 8890)
        activeServer = server
        server.start()

        server.stop()
        server.stop()
        assertFalse(server.isServerRunning)
    }

    // 11. Port configuration
    @Test
    fun test11_portConfiguration_usesCustomPort() {
        val customPort = 8891
        val server = TeacherClassroomServer(port = customPort)
        activeServer = server

        assertEquals(customPort, server.port)
        server.start()
        assertEquals(customPort, server.activePort)
    }

    // 12. Invalid/occupied port handled cleanly
    @Test
    fun test12_occupiedPort_failsCleanlyWithoutCrash() {
        val port = 8892
        val occupiedSocket = ServerSocket(port)

        val server = TeacherClassroomServer(port = port)
        activeServer = server

        val started = server.start()
        assertFalse("Server start must return false when port is occupied", started)
        assertFalse("isServerRunning must be false when port start fails", server.isServerRunning)

        occupiedSocket.close()
    }

    // 13. Health endpoint behavior (GET /health -> HTTP 200 OK)
    @Test
    fun test13_healthEndpoint_returns200OK() {
        val port = 8893
        val server = TeacherClassroomServer(port = port)
        activeServer = server
        assertTrue(server.start())

        val url = URL("http://127.0.0.1:$port/health")
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 2000
        conn.readTimeout = 2000

        val code = conn.responseCode
        assertEquals(200, code)

        val body = BufferedReader(InputStreamReader(conn.inputStream)).readText()
        assertTrue(body.contains("\"status\":\"ok\""))
        assertTrue(body.contains("\"service\":\"palash-classroom-server\""))
        conn.disconnect()
    }

    // 14. Latest endpoint returns serialized ClassroomTranslationMessage
    @Test
    fun test14_latestEndpoint_returnsSerializedMessage() {
        val port = 8894
        val server = TeacherClassroomServer(port = port)
        activeServer = server
        assertTrue(server.start())

        val msg = createValidMessage(messageId = "http_test_msg", hindiText = "TEST_HINDI")
        server.publishMessage(msg)

        val url = URL("http://127.0.0.1:$port/classroom/latest")
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 2000
        conn.readTimeout = 2000

        val code = conn.responseCode
        assertEquals(200, code)

        val body = BufferedReader(InputStreamReader(conn.inputStream)).readText()
        val deserialized = ClassroomTranslationMessage.fromJson(body, gson)
        assertNotNull(deserialized)
        assertEquals("http_test_msg", deserialized?.messageId)
        assertEquals("TEST_HINDI", deserialized?.hindiText)
        conn.disconnect()
    }

    // 15. Latest endpoint returns 404 when no message exists
    @Test
    fun test15_latestEndpoint_returns404WhenNoMessage() {
        val port = 8895
        val server = TeacherClassroomServer(port = port)
        activeServer = server
        assertTrue(server.start())

        val url = URL("http://127.0.0.1:$port/classroom/latest")
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 2000
        conn.readTimeout = 2000

        val code = conn.responseCode
        assertEquals(404, code)

        val body = BufferedReader(InputStreamReader(conn.errorStream)).readText()
        assertTrue(body.contains("\"status\":\"no_message\""))
        conn.disconnect()
    }

    // 16. Local IPv4 address utility never returns 127.0.0.1 as a classroom address
    @Test
    fun test16_localIPv4Utility_neverReturnsLoopbackAddress() {
        val ip = ClassroomNetworkUtils.getLocalIPv4Address()
        if (ip != null) {
            assertTrue("Resolved local IPv4 must not be 127.0.0.1", ip != "127.0.0.1")
            assertFalse("Resolved local IPv4 must not start with 127.", ip.startsWith("127."))
            assertFalse("Resolved local IPv4 must not start with 169.254.", ip.startsWith("169.254."))
        }
    }

    // 17. Local IPv4 utility handles network state cleanly
    @Test
    fun test17_localIPv4Utility_handlesNetworkStateCleanly() {
        // Must execute without throwing unhandled exceptions
        val ip = ClassroomNetworkUtils.getLocalIPv4Address()
        // Result is either a valid IPv4 string or null if unattached
        if (ip != null) {
            assertTrue("IPv4 string must be non-blank", ip.isNotBlank())
        }
    }
}
