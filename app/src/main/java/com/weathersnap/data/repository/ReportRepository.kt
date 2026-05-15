package com.weathersnap.data.repository

import com.weathersnap.data.local.WeatherReportDao
import com.weathersnap.data.local.WeatherReportEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@Singleton
class ReportRepository @Inject constructor(
    private val reportDao: WeatherReportDao,
    @ApplicationContext private val context: Context
) {
    suspend fun saveReport(
        cityName: String,
        temperature: Double,
        humidity: Int,
        windSpeed: Double,
        pressure: Double,
        weatherCode: Int,
        notes: String,
        tempImagePath: String
    ) = withContext(Dispatchers.IO) {
        val originalFile = File(tempImagePath)
        val originalSize = originalFile.length()
        
        // Compress image
        val bitmap = BitmapFactory.decodeFile(tempImagePath)
        val compressedFile = File(context.filesDir, "report_${UUID.randomUUID()}.jpg")
        val outputStream = FileOutputStream(compressedFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        outputStream.flush()
        outputStream.close()
        val compressedSize = compressedFile.length()
        
        // Cleanup temp file
        originalFile.delete()
        
        val report = WeatherReportEntity(
            cityName = cityName,
            temperature = temperature,
            humidity = humidity,
            windSpeed = windSpeed,
            pressure = pressure,
            weatherCode = weatherCode,
            notes = notes,
            imagePath = compressedFile.absolutePath,
            originalImageSize = originalSize,
            compressedImageSize = compressedSize,
            timestamp = System.currentTimeMillis()
        )
        reportDao.insertReport(report)
    }

    suspend fun getAllReports(): List<WeatherReportEntity> {
        return reportDao.getAllReports()
    }
}
