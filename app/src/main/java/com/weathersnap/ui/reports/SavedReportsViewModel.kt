package com.weathersnap.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weathersnap.data.local.WeatherReportEntity
import com.weathersnap.data.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SavedReportsState(
    val reports: List<WeatherReportEntity> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class SavedReportsViewModel @Inject constructor(
    private val repository: ReportRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavedReportsState())
    val uiState: StateFlow<SavedReportsState> = _uiState.asStateFlow()

    fun loadReports() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val reports = repository.getAllReports()
            _uiState.update { it.copy(reports = reports, isLoading = false) }
        }
    }
}
