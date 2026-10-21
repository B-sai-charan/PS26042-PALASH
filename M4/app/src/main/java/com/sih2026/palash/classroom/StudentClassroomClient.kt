package com.sih2026.palash.classroom

import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Result wrapper for Student classroom polling network requests.
 */
sealed class ClassroomFetchResult {
    data class Success(val message: ClassroomTranslationMessage) : ClassroomFetchResult()
    object NoMessage : ClassroomFetchResult()
    data class ServerError(val code: Int, val message: String) : ClassroomFetchResult()
    data class NetworkError(val exception: Throwable) : ClassroomFetchResult()
}

/**
 * Lightweight HTTP client for Student devices to query the Teacher's local classroom server.
 * Uses standard HttpURLConnection, works 100% offline on the local network.
 */
class StudentClassroomClient(
    private val gson: Gson = Gson(),
    private val timeoutMs: Int = 3000
) {

    companion object {
        private const val TAG = "StudentClassroomClient"

        private fun logWarn(msg: String) {
            try {
                android.util.Log.w(TAG, msg)
            } catch (_: Throwable) {
                println("[$TAG] WARN: $msg")
            }
        }
    }

    /**
     * Checks if the Teacher classroom server is online and responding at [teacherIp]:[port].
     */
    fun checkHealth(teacherIp: String, port: Int): Boolean {
        if (teacherIp.isBlank() || port <= 0) return false
        val urlStr = "http://$teacherIp:$port/health"
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(urlStr)
            conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = timeoutMs
            conn.readTimeout = timeoutMs
            conn.requestMethod = "GET"
            val code = conn.responseCode
            code == 200
        } catch (e: Exception) {
            false
        } finally {
            conn?.disconnect()
        }
    }

    /**
     * Fetches the latest published classroom translation message from [teacherIp]:[port].
     * Returns a [ClassroomFetchResult] detailing success, no message, or error.
     */
    fun fetchLatestMessage(teacherIp: String, port: Int): ClassroomFetchResult {
        if (teacherIp.isBlank() || port <= 0) {
            return ClassroomFetchResult.ServerError(400, "Invalid IP or Port")
        }

        val urlStr = "http://$teacherIp:$port/classroom/latest"
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(urlStr)
            conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = timeoutMs
            conn.readTimeout = timeoutMs
            conn.requestMethod = "GET"
            conn.setRequestProperty("Accept", "application/json")

            val responseCode = conn.responseCode
            when (responseCode) {
                200 -> {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8))
                    val json = reader.use { it.readText() }
                    val message = ClassroomTranslationMessage.fromJson(json, gson)
                    if (message != null && ClassroomMessageValidator.isValid(message)) {
                        ClassroomFetchResult.Success(message)
                    } else {
                        logWarn("Received invalid message payload: $json")
                        ClassroomFetchResult.ServerError(422, "Invalid message format")
                    }
                }
                404 -> ClassroomFetchResult.NoMessage
                else -> ClassroomFetchResult.ServerError(responseCode, "HTTP $responseCode")
            }
        } catch (e: Exception) {
            ClassroomFetchResult.NetworkError(e)
        } finally {
            conn?.disconnect()
        }
    }
}
