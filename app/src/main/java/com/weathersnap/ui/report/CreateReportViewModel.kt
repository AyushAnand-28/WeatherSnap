package com.weathersnap.ui.report

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weathersnap.data.remote.CurrentWeather
import com.weathersnap.data.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.gson.Gson
import android.net.Uri

data class CreateReportState(
    val cityName: String = "",
    val weather: CurrentWeather? = null,
    val notes: String = "",
    val tempImagePath: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val saveError: String? = null
)

@HiltViewModel
class CreateReportViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: ReportRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateReportState())
    val uiState: StateFlow<CreateReportState> = _uiState.asStateFlow()

    init {
        // Read initial args from navigation
        val cityName: String = savedStateHandle.get<String>("cityName") ?: ""
        val weatherJson: String = savedStateHandle.get<String>("weatherJson") ?: ""
        val weather = try {
            Gson().fromJson(Uri.decode(weatherJson), CurrentWeather::class.java)
        } catch (e: Exception) { null }

        // Read preserved state for challenge
        val savedNotes = savedStateHandle.get<String>("notes") ?: ""
        val savedTempImagePath = savedStateHandle.get<String>("tempImagePath")

        _uiState.update { 
            it.copy(
                cityName = cityName,
                weather = weather,
                notes = savedNotes,
                tempImagePath = savedTempImagePath
            ) 
        }
    }

    fun onNotesChange(notes: String) {
        savedStateHandle["notes"] = notes
        _uiState.update { it.copy(notes = notes) }
    }

    fun onImageCaptured(path: String) {
        savedStateHandle["tempImagePath"] = path
        _uiState.update { it.copy(tempImagePath = path) }
    }
    
    fun removeImage() {
        savedStateHandle.remove<String>("tempImagePath")
        _uiState.update { it.copy(tempImagePath = null) }
    }

    fun saveReport() {
        val state = _uiState.value
        val weather = state.weather
        if (weather == null || state.tempImagePath == null) return

        _uiState.update { it.copy(isSaving = true, saveError = null) }
        viewModelScope.launch {
            try {
                repository.saveReport(
                    cityName = state.cityName,
                    temperature = weather.temperature,
                    humidity = weather.humidity,
                    windSpeed = weather.windSpeed,
                    pressure = weather.pressure,
                    weatherCode = weather.weatherCode,
                    notes = state.notes,
                    tempImagePath = state.tempImagePath
                )
                
                // Clear state handle since it's saved
                savedStateHandle.remove<String>("notes")
                savedStateHandle.remove<String>("tempImagePath")
                
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isSaving = false, saveError = e.message ?: "Failed to save report") 
                }
            }
        }
    }
}
