package com.phoneaccessqr.app.database

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Delete
import com.phoneaccessqr.app.model.PermissionLevel

@Entity(tableName = "access_permissions")
data class PermissionEntity(
    @PrimaryKey val id: String,
    val deviceId: String,
    val deviceName: String,
    val qrCode: String,
    val permissionLevel: String,
    val createdAt: Long,
    val expiresAt: Long,
    val isActive: Boolean
)

@Dao
interface PermissionDao {
    @Insert
    suspend fun insertPermission(permission: PermissionEntity)

    @Query("SELECT * FROM access_permissions WHERE id = :id")
    suspend fun getPermissionById(id: String): PermissionEntity?

    @Query("SELECT * FROM access_permissions WHERE deviceId = :deviceId AND isActive = 1")
    suspend fun getActivePermissionsByDevice(deviceId: String): List<PermissionEntity>

    @Query("SELECT * FROM access_permissions WHERE isActive = 1")
    suspend fun getAllActivePermissions(): List<PermissionEntity>

    @Query("SELECT * FROM access_permissions ORDER BY createdAt DESC")
    suspend fun getAllPermissions(): List<PermissionEntity>

    @Query("UPDATE access_permissions SET isActive = 0 WHERE id = :id")
    suspend fun revokePermission(id: String)

    @Query("UPDATE access_permissions SET isActive = 0 WHERE expiresAt < :currentTime")
    suspend fun expireOldPermissions(currentTime: Long)

    @Delete
    suspend fun deletePermission(permission: PermissionEntity)

    @Query("DELETE FROM access_permissions WHERE expiresAt < :currentTime")
    suspend fun deleteExpiredPermissions(currentTime: Long)
}

@Entity(tableName = "access_logs")
data class AccessLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val permissionId: String,
    val timestamp: Long,
    val action: String,
    val status: String,
    val sourceDevice: String
)

@Dao
interface AccessLogDao {
    @Insert
    suspend fun insertLog(log: AccessLogEntity)

    @Query("SELECT * FROM access_logs WHERE permissionId = :permissionId ORDER BY timestamp DESC")
    suspend fun getLogsByPermission(permissionId: String): List<AccessLogEntity>

    @Query("SELECT * FROM access_logs ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentLogs(limit: Int): List<AccessLogEntity>

    @Query("DELETE FROM access_logs WHERE timestamp < :cutoffTime")
    suspend fun deleteOldLogs(cutoffTime: Long)
}

@Entity(tableName = "paired_devices")
data class PairedDeviceEntity(
    @PrimaryKey val deviceId: String,
    val deviceName: String,
    val lastAccessTime: Long,
    val trustLevel: String
)

@Dao
interface PairedDeviceDao {
    @Insert
    suspend fun insertDevice(device: PairedDeviceEntity)

    @Query("SELECT * FROM paired_devices WHERE deviceId = :deviceId")
    suspend fun getDeviceById(deviceId: String): PairedDeviceEntity?

    @Query("SELECT * FROM paired_devices ORDER BY lastAccessTime DESC")
    suspend fun getAllPairedDevices(): List<PairedDeviceEntity>

    @Query("UPDATE paired_devices SET lastAccessTime = :currentTime WHERE deviceId = :deviceId")
    suspend fun updateLastAccessTime(deviceId: String, currentTime: Long)

    @Delete
    suspend fun removeDevice(device: PairedDeviceEntity)
}
