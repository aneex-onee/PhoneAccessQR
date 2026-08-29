package com.phoneaccessqr.app

import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.phoneaccessqr.app.services.ScreenCaptureService
import com.phoneaccessqr.app.services.SocketService
import com.phoneaccessqr.app.utils.PreferenceManager

class ScreenSharingActivity : AppCompatActivity() {

    private var deviceId: String? = null
    private var deviceName: String? = null
    private var ipAddress: String? = null
    private var port: Int = 0
    private var token: String? = null
    private var isStreaming = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen_sharing)

        // Get device info from intent
        deviceId = intent.getStringExtra("device_id")
        deviceName = intent.getStringExtra("device_name")
        ipAddress = intent.getStringExtra("ip_address")
        port = intent.getIntExtra("port", 0)
        token = intent.getStringExtra("token")

        val deviceInfoText = findViewById<TextView>(R.id.device_info_text)
        val connectionStatusText = findViewById<TextView>(R.id.connection_status_text)
        val startStreamButton = findViewById<Button>(R.id.start_stream_button)
        val stopStreamButton = findViewById<Button>(R.id.stop_stream_button)
        val disconnectButton = findViewById<Button>(R.id.disconnect_button)

        // Display device info
        deviceInfoText.text = """
            Connected to: $deviceName
            IP: $ipAddress:$port
        """.trimIndent()

        connectionStatusText.text = "Ready to stream"

        startStreamButton.setOnClickListener {
            startScreenSharing()
            connectionStatusText.text = "Streaming..."
            isStreaming = true
        }

        stopStreamButton.setOnClickListener {
            stopScreenSharing()
            connectionStatusText.text = "Stopped"
            isStreaming = false
        }

        disconnectButton.setOnClickListener {
            if (isStreaming) {
                stopScreenSharing()
            }
            finish()
        }
    }

    private fun startScreenSharing() {
        // Request screen capture permission
        val mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(
            mediaProjectionManager.createScreenCaptureIntent(),
            SCREEN_CAPTURE_REQUEST_CODE
        )
    }

    private fun stopScreenSharing() {
        val intent = Intent(this, ScreenCaptureService::class.java)
        stopService(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SCREEN_CAPTURE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            val mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)

            // Start screen capture service
            val screenCaptureIntent = Intent(this, ScreenCaptureService::class.java)
            startService(screenCaptureIntent)
        }
    }

    companion object {
        private const val SCREEN_CAPTURE_REQUEST_CODE = 100
    }
}
