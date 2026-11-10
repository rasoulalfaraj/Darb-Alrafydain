package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_places")
data class FavoritePlaceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nameAr: String,
    val governorateAr: String,
    val categoryAr: String,
    val addressAr: String,
    val latitude: Double,
    val longitude: Double,
    val notesAr: String = "",
    val isOfflineReady: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "navigation_history")
data class NavigationHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val originNameAr: String,
    val destinationNameAr: String,
    val governorateAr: String,
    val destLat: Double,
    val destLng: Double,
    val distanceKm: Double,
    val durationMinutes: Int,
    val highwayNameAr: String,
    val wasOfflineMode: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "offline_governorates")
data class OfflineGovernorateEntity(
    @PrimaryKey val code: String,
    val nameAr: String,
    val capitalAr: String,
    val centerLat: Double,
    val centerLng: Double,
    val sizeMb: Int,
    val landmarksCount: Int,
    val roadNetworkKm: Int,
    val isDownloaded: Boolean,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)
