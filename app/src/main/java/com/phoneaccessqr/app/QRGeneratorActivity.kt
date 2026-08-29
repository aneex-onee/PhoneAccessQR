package com.phoneaccessqr.app

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.phoneaccessqr.app.models.QRCodeData
import com.phoneaccessqr.app.utils.NetworkUtils
import com.phoneaccessqr.app.utils.PreferenceManager
import com.phoneaccessqr.app.utils.QRCodeGenerator

class QRGeneratorActivity : AppCompatActivity() {

    private var qrBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_generator)

        val qrImageView = findViewById<ImageView>(R.id.qr_code_image)
        val statusText = findViewById<TextView>(R.id.status_text)
        val regenerateButton = findViewById<Button>(R.id.regenerate_button)
        val shareButton = findViewById<Button>(R.id.share_button)
        val startServerButton = findViewById<Button>(R.id.start_server_button)

        // Generate QR Code
        generateNewQRCode(qrImageView, statusText)

        regenerateButton.setOnClickListener {
            generateNewQRCode(qrImageView, statusText)
        }

        shareButton.setOnClickListener {
            shareQRCode()
        }

        startServerButton.setOnClickListener {
            startServer()
        }
    }

    private fun generateNewQRCode(qrImageView: ImageView, statusText: TextView) {
        // Get device info
        val deviceId = PreferenceManager.getDeviceId(this)
        val deviceName = PreferenceManager.getDeviceName(this)
        val ipAddress = NetworkUtils.getLocalIpAddress(this)
        val port = NetworkUtils.getAvailablePort()
        val token = QRCodeGenerator.generateAccessToken()

        // Save port and token
        PreferenceManager.setServerPort(this, port)
        PreferenceManager.setAccessToken(this, token)

        // Create QR code data
        val qrData = QRCodeData(
            deviceId = deviceId,
            deviceName = deviceName,
            ipAddress = ipAddress,
            port = port,
            token = token,
            accessLevel = 1
        )

        // Save to preferences
        PreferenceManager.saveQRCodeData(this, qrData)

        // Generate QR code bitmap
        qrBitmap = QRCodeGenerator.generateQRCode(qrData)
        qrImageView.setImageBitmap(qrBitmap)

        statusText.text = """
            Device: $deviceName
            IP: $ipAddress
            Port: $port
            Token: ${token.take(8)}...
        """.trimIndent()
    }

    private fun shareQRCode() {
        if (qrBitmap == null) {
            return
        }

        val qrData = PreferenceManager.getQRCodeData(this) ?: return
        val shareText = """
            Phone Access QR
            Device: ${qrData.deviceName}
            IP: ${qrData.ipAddress}
            Port: ${qrData.port}
        """.trimIndent()

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Share QR Code"))
    }

    private fun startServer() {
        val intent = Intent(this, com.phoneaccessqr.app.services.SocketService::class.java)
        startService(intent)
    }
}
