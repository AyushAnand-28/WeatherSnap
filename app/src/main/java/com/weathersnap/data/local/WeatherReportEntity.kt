package com.weathersnap.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_reports")
data class WeatherReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cityName: String,
    val temperature: Double,
    val humidity: Int,
    val windSpeed: Double,
    val pressure: Double,
    val weatherCode: Int,
    val notes: String,
    val imagePath: String,
    val originalImageSize: Long,
    val compressedImageSize: Long,
    val timestamp: Long
)
