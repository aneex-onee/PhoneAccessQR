package com.phoneaccessqr.app.utils

import android.content.Context
import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.phoneaccessqr.app.models.QRCodeData
import com.google.gson.Gson

object QRCodeGenerator {

    private const val QR_CODE_SIZE = 512

    /**
     * Generate QR code bitmap from QRCodeData
     */
    fun generateQRCode(qrData: QRCodeData): Bitmap? {
        return try {
            val json = Gson().toJson(qrData)
            val bitMatrix = MultiFormatWriter().encode(
                json,
                BarcodeFormat.QR_CODE,
                QR_CODE_SIZE,
                QR_CODE_SIZE
            )
            createBitmapFromBitMatrix(bitMatrix)
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generate QR code from raw string
     */
    fun generateQRCodeFromString(data: String): Bitmap? {
        return try {
            val bitMatrix = MultiFormatWriter().encode(
                data,
                BarcodeFormat.QR_CODE,
                QR_CODE_SIZE,
                QR_CODE_SIZE
            )
            createBitmapFromBitMatrix(bitMatrix)
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Convert BitMatrix to Bitmap
     */
    private fun createBitmapFromBitMatrix(bitMatrix: BitMatrix): Bitmap {
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
            }
        }
        return bitmap
    }

    /**
     * Generate access token for security
     */
    fun generateAccessToken(): String {
        return java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 32)
    }

    /**
     * Validate QR code format
     */
    fun validateQRCodeData(json: String): Boolean {
        return try {
            val qrData = Gson().fromJson(json, QRCodeData::class.java)
            qrData.deviceId.isNotEmpty() &&
                    qrData.ipAddress.isNotEmpty() &&
                    qrData.port > 0 &&
                    qrData.token.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
}
