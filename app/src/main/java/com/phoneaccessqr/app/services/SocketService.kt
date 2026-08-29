package com.phoneaccessqr.app.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.google.gson.Gson
import com.phoneaccessqr.app.models.SocketMessage
import com.phoneaccessqr.app.utils.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

class SocketService : Service() {

    private val binder = LocalBinder()
    private var serverSocket: ServerSocket? = null
    private var isRunning = false
    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.IO)
    private val connectedClients = mutableListOf<Socket>()
    private var messageListener: ((SocketMessage) -> Unit)? = null

    inner class LocalBinder : Binder() {
        fun getService(): SocketService = this@SocketService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    /**
     * Start socket server
     */
    fun startServer(port: Int = 9999) {
        scope.launch {
            try {
                serverSocket = ServerSocket(port)
                isRunning = true
                Log.d("SocketService", "Server started on port $port")

                while (isRunning) {
                    val clientSocket = serverSocket?.accept()
                    if (clientSocket != null) {
                        connectedClients.add(clientSocket)
                        handleClient(clientSocket)
                    }
                }
            } catch (e: Exception) {
                Log.e("SocketService", "Server error: ${e.message}")
                isRunning = false
            }
        }
    }

    /**
     * Handle individual client connection
     */
    private fun handleClient(clientSocket: Socket) {
        scope.launch {
            try {
                val reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
                val writer = PrintWriter(clientSocket.getOutputStream(), true)

                var line: String?
                while (reader.readLine().also { line = it } != null && isRunning) {
                    line?.let { jsonData ->
                        try {
                            val message = gson.fromJson(jsonData, SocketMessage::class.java)
                            Log.d("SocketService", "Message received: ${message.type}")

                            // Validate message
                            if (validateMessage(message)) {
                                messageListener?.invoke(message)
                                writer.println(createResponse("success", "Message received"))
                            } else {
                                writer.println(createResponse("error", "Invalid message"))
                            }
                        } catch (e: Exception) {
                            Log.e("SocketService", "Error parsing message: ${e.message}")
                            writer.println(createResponse("error", "Invalid JSON format"))
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("SocketService", "Client error: ${e.message}")
            } finally {
                connectedClients.remove(clientSocket)
                clientSocket.close()
            }
        }
    }

    /**
     * Validate incoming message
     */
    private fun validateMessage(message: SocketMessage): Boolean {
        return message.deviceId.isNotEmpty() &&
                message.token.isNotEmpty() &&
                isValidToken(message.token)
    }

    /**
     * Validate access token
     */
    private fun isValidToken(token: String): Boolean {
        // In production, verify token against database
        return token.length >= 20
    }

    /**
     * Send message to specific client
     */
    fun sendMessage(clientSocket: Socket, message: SocketMessage) {
        scope.launch {
            try {
                val writer = PrintWriter(clientSocket.getOutputStream(), true)
                val json = gson.toJson(message)
                writer.println(json)
            } catch (e: Exception) {
                Log.e("SocketService", "Error sending message: ${e.message}")
            }
        }
    }

    /**
     * Broadcast message to all connected clients
     */
    fun broadcastMessage(message: SocketMessage) {
        scope.launch {
            connectedClients.forEach { socket ->
                sendMessage(socket, message)
            }
        }
    }

    /**
     * Connect to remote socket server
     */
    fun connectToServer(host: String, port: Int, onConnected: (Socket) -> Unit) {
        scope.launch {
            try {
                val socket = Socket(host, port)
                onConnected(socket)
                Log.d("SocketService", "Connected to server at $host:$port")
            } catch (e: Exception) {
                Log.e("SocketService", "Connection error: ${e.message}")
            }
        }
    }

    /**
     * Set message listener
     */
    fun setMessageListener(listener: (SocketMessage) -> Unit) {
        messageListener = listener
    }

    /**
     * Create response message
     */
    private fun createResponse(status: String, message: String): String {
        val response = mapOf(
            "status" to status,
            "message" to message,
            "timestamp" to System.currentTimeMillis()
        )
        return gson.toJson(response)
    }

    /**
     * Stop server and close connections
     */
    fun stopServer() {
        isRunning = false
        connectedClients.forEach { it.close() }
        connectedClients.clear()
        serverSocket?.close()
        Log.d("SocketService", "Server stopped")
    }

    /**
     * Get number of connected clients
     */
    fun getConnectedClientsCount(): Int = connectedClients.size

    override fun onDestroy() {
        super.onDestroy()
        stopServer()
    }
}
