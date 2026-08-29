package com.phoneaccessqr.app.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.phoneaccessqr.app.models.AccessPermission
import kotlinx.coroutines.flow.Flow

@Dao
interface AccessPermissionDao {
    
    @Insert
    suspend fun insertPermission(permission: AccessPermission): Long

    @Update
    suspend fun updatePermission(permission: AccessPermission)

    @Delete
    suspend fun deletePermission(permission: AccessPermission)

    @Query("SELECT * FROM access_permissions WHERE id = :id")
    suspend fun getPermissionById(id: Int): AccessPermission?

    @Query("SELECT * FROM access_permissions WHERE deviceId = :deviceId")
    suspend fun getPermissionByDeviceId(deviceId: String): AccessPermission?

    @Query("SELECT * FROM access_permissions WHERE isActive = 1")
    fun getAllActivePermissions(): Flow<List<AccessPermission>>

    @Query("SELECT * FROM access_permissions")
    fun getAllPermissions(): Flow<List<AccessPermission>>

    @Query("UPDATE access_permissions SET isActive = 0 WHERE id = :id")
    suspend fun revokePermission(id: Int)

    @Query("DELETE FROM access_permissions WHERE expiresAt < :currentTime")
    suspend fun deleteExpiredPermissions(currentTime: Long)

    @Query("SELECT COUNT(*) FROM access_permissions WHERE isActive = 1")
    fun getActivePermissionCount(): Flow<Int>
}
