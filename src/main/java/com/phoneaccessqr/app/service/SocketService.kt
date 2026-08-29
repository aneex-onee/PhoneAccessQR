package com.phoneaccessqr.app.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.UUID

class SocketService : Service() {
    private val tag = "SocketService"
    private val binder = LocalBinder()
    private var serverSocket: ServerSocket? = null
    private var isRunning = false
    private val connectedClients = mutableMapOf<String, ClientConnection>()

    inner class LocalBinder : Binder() {
        fun getService(): SocketService = this@SocketService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startSocketServer()
        return START_STICKY
    }

    private fun startSocketServer(port: Int = 5000) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                serverSocket = ServerSocket(port)
                isRunning = true
                Log.d(tag, "Socket server started on port $port")

                while (isRunning) {
                    val clientSocket = serverSocket?.accept()
                    if (clientSocket != null) {
                        handleClient(clientSocket)
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Socket server error: ${e.message}")
            }
        }
    }

    private fun handleClient(clientSocket: Socket) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val clientId = UUID.randomUUID().toString()
                val connection = ClientConnection(clientSocket, clientId)
                connectedClients[clientId] = connection

                val reader = BufferedReader(InputStreamReader(clientSocket.inputStream))
                val writer = BufferedWriter(OutputStreamWriter(clientSocket.outputStream))

                // Send initial handshake
                writer.write("CONNECTED:$clientId\n")
                writer.flush()

                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    handleCommand(line!!, clientId, writer)
                }

                connectedClients.remove(clientId)
                clientSocket.close()
            } catch (e: Exception) {
                Log.e(tag, "Client handler error: ${e.message}")
            }
        }
    }

    private suspend fun handleCommand(
        command: String,
        clientId: String,
        writer: BufferedWriter
    ) {
        val parts = command.split(":")
        when {
            parts[0] == "GET_SCREEN" -> {
                // Request screen capture
                writer.write("SCREEN_READY\n")
                writer.flush()
            }
            parts[0] == "PERMISSION_CHECK" -> {
                // Verify permission
                val permissionLevel = parts.getOrNull(1) ?: "NONE"
                writer.write("PERMISSION:$permissionLevel\n")
                writer.flush()
            }
            parts[0] == "CONTROL_INPUT" -> {
                // Handle control input
                writer.write("INPUT_RECEIVED\n")
                writer.flush()
            }
        }
    }

    fun sendMessageToAllClients(message: String) {
        GlobalScope.launch(Dispatchers.IO) {
            connectedClients.values.forEach { connection ->
                try {
                    connection.send(message)
                } catch (e: Exception) {
                    Log.e(tag, "Error sending to client: ${e.message}")
                }
            }
        }
    }

    fun getConnectedClientsCount(): Int = connectedClients.size

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        try {
            serverSocket?.close()
        } catch (e: Exception) {
            Log.e(tag, "Error closing server socket: ${e.message}")
        }
        connectedClients.clear()
    }

    private class ClientConnection(
        private val socket: Socket,
        val id: String
    ) {
        private val writer = BufferedWriter(OutputStreamWriter(socket.outputStream))

        fun send(message: String) {
            writer.write(message + "\n")
            writer.flush()
        }
    }
}
