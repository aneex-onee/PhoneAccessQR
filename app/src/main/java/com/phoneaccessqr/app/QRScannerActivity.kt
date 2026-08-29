package com.phoneaccessqr.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.phoneaccessqr.app.utils.PreferenceManager
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class QRScannerActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var statusText: TextView
    private lateinit var connectButton: Button
    private lateinit var cameraExecutor: ExecutorService
    private var cameraProvider: ProcessCameraProvider? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_scanner)

        previewView = findViewById(R.id.preview_view)
        statusText = findViewById(R.id.scanner_status_text)
        connectButton = findViewById(R.id.connect_button)
        cameraExecutor = Executors.newSingleThreadExecutor()

        connectButton.isEnabled = false

        // Request camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        } else {
            startCamera()
        }

        connectButton.setOnClickListener {
            connectToScannedDevice()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(
            {
                cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider?.unbindAll()
                    cameraProvider?.bindToLifecycle(this, cameraSelector, preview)
                    statusText.text = "Camera started. Scanning QR codes..."
                } catch (e: Exception) {
                    statusText.text = "Camera error: ${e.message}"
                }
            },
            ContextCompat.getMainExecutor(this)
        )
    }

    private fun connectToScannedDevice() {
        val qrData = PreferenceManager.getScannedQRData(this)
        if (qrData != null) {
            // Start screen sharing activity
            val intent = android.content.Intent(this, ScreenSharingActivity::class.java)
            intent.putExtra("device_id", qrData.deviceId)
            intent.putExtra("device_name", qrData.deviceName)
            intent.putExtra("ip_address", qrData.ipAddress)
            intent.putExtra("port", qrData.port)
            intent.putExtra("token", qrData.token)
            startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            statusText.text = "Camera permission denied"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
    }
}
