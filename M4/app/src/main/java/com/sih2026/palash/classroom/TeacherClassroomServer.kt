package com.sih2026.palash.classroom

import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Lightweight local HTTP server for hosting Teacher classroom translation broadcasts.
 * Listens on a configurable local TCP port (default 8080) and operates 100% offline.
 */
class TeacherClassroomServer(
    val port: Int = DEFAULT_PORT,
    private val gson: Gson = Gson()
) {

    companion object {
        const val DEFAULT_PORT = 8080
        private const val TAG = "TeacherClassroomServer"

        private fun logInfo(msg: String) {
            try {
                android.util.Log.i(TAG, msg)
            } catch (_: Throwable) {
                println("[$TAG] INFO: $msg")
            }
        }

        private fun logWarn(msg: String) {
            try {
                android.util.Log.w(TAG, msg)
            } catch (_: Throwable) {
                println("[$TAG] WARN: $msg")
            }
        }

        private fun logError(msg: String, throwable: Throwable? = null) {
            try {
                android.util.Log.e(TAG, msg, throwable)
            } catch (_: Throwable) {
                println("[$TAG] ERROR: $msg ${throwable?.message ?: ""}")
            }
        }
    }

    private var serverSocket: ServerSocket? = null
    private val executor = Executors.newCachedThreadPool()
    private val isRunning = AtomicBoolean(false)
    private val latestMessage = AtomicReference<ClassroomTranslationMessage?>(null)

    val activePort: Int
        get() = serverSocket?.localPort ?: port

    val isServerRunning: Boolean
        get() = isRunning.get()

    @Synchronized
    fun start(): Boolean {
        if (isRunning.get()) {
            logWarn("Server is already running on port $activePort")
            return true
        }

        return try {
            val ss = ServerSocket()
            ss.reuseAddress = true
            ss.bind(InetSocketAddress(port))
            this.serverSocket = ss
            isRunning.set(true)

            executor.execute {
                acceptConnections(ss)
            }

            logInfo("TeacherClassroomServer started on port ${ss.localPort}")
            true
        } catch (e: Exception) {
            logError("Failed to start TeacherClassroomServer on port $port: ${e.message}", e)
            stopInternal()
            false
        }
    }

    @Synchronized
    fun stop() {
        stopInternal()
    }

    private fun stopInternal() {
        isRunning.set(false)
        try {
            serverSocket?.close()
        } catch (e: Exception) {
            logWarn("Error closing server socket: ${e.message}")
        }
        serverSocket = null
    }

    fun publishMessage(message: ClassroomTranslationMessage): Boolean {
        if (!ClassroomMessageValidator.isValid(message)) {
            logWarn("Rejected invalid message: $message")
            return false
        }
        latestMessage.set(message)
        logInfo("Published message ${message.messageId} to classroom server.")
        return true
    }

    fun getLatestMessage(): ClassroomTranslationMessage? {
        return latestMessage.get()
    }

    fun clearMessages() {
        latestMessage.set(null)
    }

    private fun acceptConnections(ss: ServerSocket) {
        while (isRunning.get() && !ss.isClosed) {
            try {
                val clientSocket = ss.accept()
                executor.execute {
                    handleClientRequest(clientSocket)
                }
            } catch (e: Exception) {
                if (isRunning.get()) {
                    logWarn("Accept loop stopped or error: ${e.message}")
                }
            }
        }
    }

    private fun handleClientRequest(socket: Socket) {
        try {
            socket.soTimeout = 3000
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val requestLine = reader.readLine() ?: return

            val parts = requestLine.split(" ")
            if (parts.size < 2) {
                sendResponse(socket, 400, "application/json", "{\"status\":\"bad_request\"}")
                return
            }

            val method = parts[0]
            val path = parts[1]

            if (method != "GET") {
                sendResponse(socket, 405, "application/json", "{\"status\":\"method_not_allowed\"}")
                return
            }

            when {
                path == "/health" || path.startsWith("/health?") -> {
                    val response = "{\"status\":\"ok\",\"service\":\"palash-classroom-server\"}"
                    sendResponse(socket, 200, "application/json", response)
                }
                path == "/classroom/latest" || path.startsWith("/classroom/latest?") -> {
                    val msg = latestMessage.get()
                    if (msg != null) {
                        sendResponse(socket, 200, "application/json", msg.toJson(gson))
                    } else {
                        sendResponse(socket, 404, "application/json", "{\"status\":\"no_message\"}")
                    }
                }
                else -> {
                    sendResponse(socket, 404, "application/json", "{\"status\":\"not_found\"}")
                }
            }
        } catch (e: Exception) {
            logWarn("Error handling client request: ${e.message}")
        } finally {
            try {
                socket.close()
            } catch (_: Exception) {}
        }
    }

    private fun sendResponse(socket: Socket, statusCode: Int, contentType: String, body: String) {
        val out: OutputStream = socket.getOutputStream()
        val statusText = when (statusCode) {
            200 -> "OK"
            400 -> "Bad Request"
            404 -> "Not Found"
            405 -> "Method Not Allowed"
            else -> "Internal Error"
        }
        val bodyBytes = body.toByteArray(Charsets.UTF_8)
        val headers = "HTTP/1.1 $statusCode $statusText\r\n" +
                "Content-Type: $contentType; charset=UTF-8\r\n" +
                "Content-Length: ${bodyBytes.size}\r\n" +
                "Connection: close\r\n\r\n"
        out.write(headers.toByteArray(Charsets.UTF_8))
        out.write(bodyBytes)
        out.flush()
    }
}
