package com.phoneaccessqr.app.service

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.phoneaccessqr.app.database.AppDatabase
import com.phoneaccessqr.app.database.PermissionEntity
import com.phoneaccessqr.app.model.AccessStatus
import com.phoneaccessqr.app.model.PermissionLevel
import com.phoneaccessqr.app.database.AccessLogEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PermissionManagerService(private val context: Context) {
    private val tag = "PermissionManager"
    private val database = AppDatabase.getDatabase(context)
    private val permissionDao = database.permissionDao()
    private val accessLogDao = database.accessLogDao()

    suspend fun grantPermission(
        deviceId: String,
        deviceName: String,
        qrCode: String,
        permissionLevel: PermissionLevel,
        expiryMinutes: Int = 60
    ): String = withContext(Dispatchers.IO) {
        try {
            val permissionId = generatePermissionId()
            val now = System.currentTimeMillis()
            val expiryTime = now + (expiryMinutes * 60 * 1000)

            val permission = PermissionEntity(
                id = permissionId,
                deviceId = deviceId,
                deviceName = deviceName,
                qrCode = qrCode,
                permissionLevel = permissionLevel.name,
                createdAt = now,
                expiresAt = expiryTime,
                isActive = true
            )

            permissionDao.insertPermission(permission)
            Log.d(tag, "Permission granted: $permissionId")
            permissionId
        } catch (e: Exception) {
            Log.e(tag, "Error granting permission: ${e.message}")
            ""
        }
    }

    suspend fun revokePermission(permissionId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            permissionDao.revokePermission(permissionId)
            logAccess(permissionId, "REVOKE", AccessStatus.SUCCESS)
            Log.d(tag, "Permission revoked: $permissionId")
            true
        } catch (e: Exception) {
            Log.e(tag, "Error revoking permission: ${e.message}")
            false
        }
    }

    suspend fun checkPermission(permissionId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val permission = permissionDao.getPermissionById(permissionId)
            
            if (permission == null) {
                logAccess(permissionId, "CHECK", AccessStatus.DENIED)
                return@withContext false
            }

            val isValid = permission.isActive && !hasExpired(permission.expiresAt)
            
            if (!isValid) {
                logAccess(permissionId, "CHECK", AccessStatus.EXPIRED)
            } else {
                logAccess(permissionId, "CHECK", AccessStatus.SUCCESS)
            }

            isValid
        } catch (e: Exception) {
            Log.e(tag, "Error checking permission: ${e.message}")
            logAccess(permissionId, "CHECK", AccessStatus.FAILED)
            false
        }
    }

    suspend fun getPermissionLevel(permissionId: String): PermissionLevel? = withContext(Dispatchers.IO) {
        return@withContext try {
            val permission = permissionDao.getPermissionById(permissionId)
            permission?.let {
                PermissionLevel.valueOf(it.permissionLevel)
            }
        } catch (e: Exception) {
            Log.e(tag, "Error getting permission level: ${e.message}")
            null
        }
    }

    suspend fun getAllActivePermissions(): List<PermissionEntity> = withContext(Dispatchers.IO) {
        return@withContext try {
            permissionDao.getAllActivePermissions()
        } catch (e: Exception) {
            Log.e(tag, "Error getting active permissions: ${e.message}")
            emptyList()
        }
    }

    suspend fun expireOldPermissions() = withContext(Dispatchers.IO) {
        try {
            permissionDao.expireOldPermissions(System.currentTimeMillis())
            Log.d(tag, "Expired old permissions")
        } catch (e: Exception) {
            Log.e(tag, "Error expiring permissions: ${e.message}")
        }
    }

    suspend fun getAccessLogs(permissionId: String) = withContext(Dispatchers.IO) {
        return@withContext try {
            accessLogDao.getLogsByPermission(permissionId)
        } catch (e: Exception) {
            Log.e(tag, "Error getting access logs: ${e.message}")
            emptyList()
        }
    }

    private suspend fun logAccess(
        permissionId: String,
        action: String,
        status: AccessStatus
    ) {
        try {
            val log = AccessLogEntity(
                permissionId = permissionId,
                timestamp = System.currentTimeMillis(),
                action = action,
                status = status.name,
                sourceDevice = android.os.Build.DEVICE
            )
            accessLogDao.insertLog(log)
        } catch (e: Exception) {
            Log.e(tag, "Error logging access: ${e.message}")
        }
    }

    private fun hasExpired(expiryTime: Long): Boolean {
        return System.currentTimeMillis() > expiryTime
    }

    private fun generatePermissionId(): String {
        return "perm_" + System.currentTimeMillis() + "_" + kotlin.random.Random.nextInt(10000)
    }
}
