package com.weathersnap.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WeatherReportDao {
    @Insert
    suspend fun insertReport(report: WeatherReportEntity)

    @Query("SELECT * FROM weather_reports ORDER BY timestamp DESC")
    suspend fun getAllReports(): List<WeatherReportEntity>
}
