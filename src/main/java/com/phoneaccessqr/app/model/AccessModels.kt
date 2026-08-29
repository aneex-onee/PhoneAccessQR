package com.phoneaccessqr.app.model

data class AccessPermission(
    val id: String,
    val deviceId: String,
    val deviceName: String,
    val qrCode: String,
    val permissionLevel: PermissionLevel,
    val createdAt: Long,
    val expiresAt: Long,
    val isActive: Boolean,
    val accessHistory: List<AccessLog> = emptyList()
)

enum class PermissionLevel {
    VIEW_ONLY,        // Can only view screen
    CONTROL,          // Can view and control
    FULL_ACCESS       // Full device access
}

data class AccessLog(
    val timestamp: Long,
    val action: String,
    val status: AccessStatus
)

enum class AccessStatus {
    SUCCESS,
    FAILED,
    DENIED,
    EXPIRED
}

data class DeviceInfo(
    val deviceId: String,
    val deviceName: String,
    val osVersion: Int,
    val isAccessGranted: Boolean,
    val lastAccessTime: Long
)

data class QRCodeData(
    val deviceId: String,
    val deviceName: String,
    val ipAddress: String,
    val port: Int,
    val secretKey: String,
    val permissionLevel: PermissionLevel,
    val expiryTime: Long
)
