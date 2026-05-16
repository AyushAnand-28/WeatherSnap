package com.weathersnap.ui.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weathersnap.data.remote.CurrentWeather
import com.weathersnap.data.remote.GeocodingResult
import com.weathersnap.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WeatherUiState(
    val query: String = "",
    val suggestions: List<GeocodingResult> = emptyList(),
    val isSearching: Boolean = false,
    val selectedCity: GeocodingResult? = null,
    val weather: CurrentWeather? = null,
    val isLoadingWeather: Boolean = false,
    val weatherError: String? = null
)

@OptIn(FlowPreview::class)
@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val searchQuery = MutableStateFlow("")

    init {
        searchQuery
            .debounce(300)
            .onEach { query ->
                if (query.length > 2) {
                    _uiState.update { it.copy(isSearching = true) }
                    val results = repository.searchCity(query)
                    _uiState.update { it.copy(suggestions = results, isSearching = false) }
                } else {
                    _uiState.update { it.copy(suggestions = emptyList(), isSearching = false) }
                }
            }
            .launchIn(viewModelScope)
    }

    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(query = newQuery, selectedCity = null, weather = null, weatherError = null) }
        searchQuery.value = newQuery
    }

    fun onCitySelected(city: GeocodingResult) {
        _uiState.update { 
            it.copy(
                query = city.name, 
                selectedCity = city,
                suggestions = emptyList(),
                isLoadingWeather = true,
                weatherError = null
            )
        }
        
        viewModelScope.launch {
            val result = repository.getCurrentWeather(city.latitude, city.longitude)
            if (result.isSuccess) {
                _uiState.update { it.copy(weather = result.getOrNull(), isLoadingWeather = false) }
            } else {
                _uiState.update { 
                    it.copy(
                        weatherError = result.exceptionOrNull()?.message ?: "Failed to load weather", 
                        isLoadingWeather = false
                    ) 
                }
            }
        }
    }
}
