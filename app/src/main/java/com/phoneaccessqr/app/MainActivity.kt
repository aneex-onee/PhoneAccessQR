package com.phoneaccessqr.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.phoneaccessqr.app.utils.NetworkUtils
import com.phoneaccessqr.app.utils.PreferenceManager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        val deviceNameText = findViewById<TextView>(R.id.device_name_text)
        val ipAddressText = findViewById<TextView>(R.id.ip_address_text)
        val generateQRButton = findViewById<Button>(R.id.generate_qr_button)
        val scanQRButton = findViewById<Button>(R.id.scan_qr_button)
        val permissionButton = findViewById<Button>(R.id.permission_button)

        // Get and display device info
        val deviceId = PreferenceManager.getDeviceId(this)
        val deviceName = PreferenceManager.getDeviceName(this)
        val ipAddress = NetworkUtils.getLocalIpAddress(this)

        deviceNameText.text = "Device: $deviceName\nID: $deviceId"
        ipAddressText.text = "IP: $ipAddress"

        // Save IP address
        PreferenceManager.setLocalIP(this, ipAddress)

        // Generate QR Code Button
        generateQRButton.setOnClickListener {
            startActivity(Intent(this, QRGeneratorActivity::class.java))
        }

        // Scan QR Code Button
        scanQRButton.setOnClickListener {
            startActivity(Intent(this, QRScannerActivity::class.java))
        }

        // Permission Management Button
        permissionButton.setOnClickListener {
            startActivity(Intent(this, PermissionManagementActivity::class.java))
        }
    }
}
