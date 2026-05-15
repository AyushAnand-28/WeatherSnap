package com.weathersnap.data.repository

import com.weathersnap.data.remote.GeocodingApi
import com.weathersnap.data.remote.WeatherApi
import com.weathersnap.data.remote.GeocodingResult
import com.weathersnap.data.remote.CurrentWeather
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val geocodingApi: GeocodingApi,
    private val weatherApi: WeatherApi
) {
    private val cityCache = mutableMapOf<String, List<GeocodingResult>>()

    suspend fun searchCity(query: String): List<GeocodingResult> {
        if (query.length <= 2) return emptyList()
        cityCache[query]?.let { return it }
        
        return try {
            val response = geocodingApi.searchCity(name = query)
            val results = response.results ?: emptyList()
            cityCache[query] = results
            results
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getCurrentWeather(lat: Double, lon: Double): Result<CurrentWeather> {
        return try {
            val res = weatherApi.getCurrentWeather(latitude = lat, longitude = lon).current
            Result.success(res)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
