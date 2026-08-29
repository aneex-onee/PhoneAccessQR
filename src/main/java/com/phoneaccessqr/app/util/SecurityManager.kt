package com.phoneaccessqr.app.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.util.UUID

object SecurityManager {
    private const val PREFS_FILE = "secure_prefs"
    private const val KEY_DEVICE_ID = "device_id"
    private const val KEY_DEVICE_NAME = "device_name"
    private const val KEY_SECRET_KEY = "secret_key"

    private fun getEncryptedSharedPreferences(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getOrCreateDeviceId(context: Context): String {
        val prefs = getEncryptedSharedPreferences(context)
        var deviceId = prefs.getString(KEY_DEVICE_ID, null)

        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
        }

        return deviceId
    }

    fun setDeviceName(context: Context, name: String) {
        val prefs = getEncryptedSharedPreferences(context)
        prefs.edit().putString(KEY_DEVICE_NAME, name).apply()
    }

    fun getDeviceName(context: Context): String {
        val prefs = getEncryptedSharedPreferences(context)
        return prefs.getString(KEY_DEVICE_NAME, "Unknown Device") ?: "Unknown Device"
    }

    fun generateSecretKey(): String {
        return UUID.randomUUID().toString()
    }

    fun verifySecretKey(providedKey: String, storedKey: String): Boolean {
        return providedKey == storedKey
    }

    fun hasExpired(expiryTime: Long): Boolean {
        return System.currentTimeMillis() > expiryTime
    }
}
