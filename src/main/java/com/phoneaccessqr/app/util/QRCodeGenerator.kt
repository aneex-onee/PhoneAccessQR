package com.phoneaccessqr.app.util

import android.content.Context
import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.phoneaccessqr.app.model.PermissionLevel
import com.phoneaccessqr.app.model.QRCodeData
import com.google.gson.Gson
import java.util.UUID

object QRCodeGenerator {
    private val multiFormatWriter = MultiFormatWriter()
    private val gson = Gson()

    fun generateQRCode(
        deviceId: String,
        deviceName: String,
        ipAddress: String,
        port: Int,
        permissionLevel: PermissionLevel = PermissionLevel.VIEW_ONLY,
        expiryMinutes: Int = 60,
        width: Int = 512,
        height: Int = 512
    ): Bitmap {
        val secretKey = generateSecretKey()
        val expiryTime = System.currentTimeMillis() + (expiryMinutes * 60 * 1000)

        val qrData = QRCodeData(
            deviceId = deviceId,
            deviceName = deviceName,
            ipAddress = ipAddress,
            port = port,
            secretKey = secretKey,
            permissionLevel = permissionLevel,
            expiryTime = expiryTime
        )

        val jsonData = gson.toJson(qrData)
        val bitMatrix = multiFormatWriter.encode(
            jsonData,
            BarcodeFormat.QR_CODE,
            width,
            height
        )

        return createBitmapFromBitMatrix(bitMatrix)
    }

    private fun createBitmapFromBitMatrix(bitMatrix: BitMatrix): Bitmap {
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }

        return bitmap
    }

    private fun generateSecretKey(): String {
        return UUID.randomUUID().toString()
    }

    fun parseQRCodeData(jsonData: String): QRCodeData? {
        return try {
            gson.fromJson(jsonData, QRCodeData::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
