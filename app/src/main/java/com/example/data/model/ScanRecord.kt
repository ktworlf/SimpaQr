package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scans")
data class ScanRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val rawContent: String,
    val formatName: String = "QR_CODE",
    val qrType: String = "TEXT",
    val title: String,
    val subtitle: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val scanSource: String = "CAMERA", // CAMERA, GALLERY, BATCH, STUDIO
    val securityStatus: String = "NEUTRAL", // SAFE, WARNING, DANGER, NEUTRAL
    val safetyNotes: String = ""
)

enum class ParsedQrType {
    URL,
    WIFI,
    CONTACT,
    EMAIL,
    PHONE,
    SMS,
    GEO,
    UPI,
    TEXT
}

data class ParsedQrData(
    val type: ParsedQrType,
    val title: String,
    val displayValue: String,
    val secondaryValue: String = "",
    val rawValue: String,
    val wifiSsid: String? = null,
    val wifiPass: String? = null,
    val wifiType: String? = null,
    val contactName: String? = null,
    val contactPhone: String? = null,
    val contactEmail: String? = null,
    val url: String? = null,
    val phoneNumber: String? = null,
    val emailAddress: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val upiPayeeId: String? = null,
    val upiPayeeName: String? = null,
    val upiAmount: String? = null
)
