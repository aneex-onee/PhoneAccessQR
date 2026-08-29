package com.phoneaccessqr.app.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "access_permissions")
data class AccessPermission(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val deviceId: String,
    val deviceName: String,
    val qrCode: String,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long? = null,
    val accessLevel: Int = 1, // 1: View Only, 2: Control, 3: Full Access
    val ipAddress: String,
    val port: Int
)

data class QRCodeData(
    val deviceId: String,
    val deviceName: String,
    val ipAddress: String,
    val port: Int,
    val token: String,
    val accessLevel: Int = 1
)

data class ScreenFrame(
    val timestamp: Long,
    val frameData: ByteArray,
    val width: Int,
    val height: Int,
    val rotation: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ScreenFrame

        if (timestamp != other.timestamp) return false
        if (!frameData.contentEquals(other.frameData)) return false
        if (width != other.width) return false
        if (height != other.height) return false
        if (rotation != other.rotation) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + frameData.contentHashCode()
        result = 31 * result + width
        result = 31 * result + height
        result = 31 * result + rotation
        return result
    }
}

data class SocketMessage(
    val type: String, // "permission_request", "permission_grant", "screen_start", "screen_data", "control_input"
    val deviceId: String,
    val token: String,
    val payload: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
