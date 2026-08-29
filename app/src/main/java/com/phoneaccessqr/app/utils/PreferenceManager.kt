package com.phoneaccessqr.app.utils

import android.content.Context
import android.provider.Settings
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.phoneaccessqr.app.models.QRCodeData
import java.util.UUID

object PreferenceManager {

    private fun getEncryptedSharedPreferences(context: Context): EncryptedSharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            "phone_access_qr_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }

    fun saveQRCodeData(context: Context, qrData: QRCodeData) {
        val prefs = getEncryptedSharedPreferences(context)
        val gson = Gson()
        val json = gson.toJson(qrData)
        prefs.edit().putString("qr_code_data", json).apply()
    }

    fun getQRCodeData(context: Context): QRCodeData? {
        val prefs = getEncryptedSharedPreferences(context)
        val json = prefs.getString("qr_code_data", null) ?: return null
        val gson = Gson()
        return gson.fromJson(json, QRCodeData::class.java)
    }

    fun getDeviceId(context: Context): String {
        val prefs = getEncryptedSharedPreferences(context)
        var deviceId = prefs.getString("device_id", null)
        
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString()
            prefs.edit().putString("device_id", deviceId).apply()
        }
        
        return deviceId
    }

    fun getDeviceName(context: Context): String {
        val prefs = getEncryptedSharedPreferences(context)
        var deviceName = prefs.getString("device_name", null)
        
        if (deviceName == null) {
            deviceName = Settings.Global.getString(
                context.contentResolver,
                Settings.Global.DEVICE_NAME
            ) ?: android.os.Build.MODEL
            prefs.edit().putString("device_name", deviceName).apply()
        }
        
        return deviceName
    }

    fun setAccessToken(context: Context, token: String) {
        val prefs = getEncryptedSharedPreferences(context)
        prefs.edit().putString("access_token", token).apply()
    }

    fun getAccessToken(context: Context): String? {
        val prefs = getEncryptedSharedPreferences(context)
        return prefs.getString("access_token", null)
    }

    fun setServerPort(context: Context, port: Int) {
        val prefs = getEncryptedSharedPreferences(context)
        prefs.edit().putInt("server_port", port).apply()
    }

    fun getServerPort(context: Context): Int {
        val prefs = getEncryptedSharedPreferences(context)
        return prefs.getInt("server_port", 9999)
    }

    fun setLocalIP(context: Context, ip: String) {
        val prefs = getEncryptedSharedPreferences(context)
        prefs.edit().putString("local_ip", ip).apply()
    }

    fun getLocalIP(context: Context): String? {
        val prefs = getEncryptedSharedPreferences(context)
        return prefs.getString("local_ip", null)
    }

    fun clear(context: Context) {
        val prefs = getEncryptedSharedPreferences(context)
        prefs.edit().clear().apply()
    }
}
