package com.weathersnap.data.remote

import com.google.gson.annotations.SerializedName
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class GeocodingResponse(
    val results: List<GeocodingResult>?
)

@Parcelize
data class GeocodingResult(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String?,
    val admin1: String?
) : Parcelable

data class WeatherResponse(
    val current: CurrentWeather
)

@Parcelize
data class CurrentWeather(
    @SerializedName("temperature_2m") val temperature: Double,
    @SerializedName("relative_humidity_2m") val humidity: Int,
    @SerializedName("weather_code") val weatherCode: Int,
    @SerializedName("wind_speed_10m") val windSpeed: Double,
    @SerializedName("surface_pressure") val pressure: Double
) : Parcelable
