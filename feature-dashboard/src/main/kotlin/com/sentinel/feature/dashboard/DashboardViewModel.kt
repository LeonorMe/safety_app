package com.sentinel.feature.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sentinel.core.security.CoreSecurityProvider
import com.sentinel.domain.model.SecurityStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val securityProvider: CoreSecurityProvider,
    private val appScanner: AppScanner,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        refreshSecurityStatus()
    }

    fun refreshSecurityStatus() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            
            val isRooted = securityProvider.isRooted()
            val isUsbDebug = securityProvider.isUsbDebuggingEnabled(context)
            val isUnknownSources = securityProvider.isUnknownSourcesEnabled(context)
            val patchLevel = securityProvider.getSecurityPatchLevel()
            
            // Perform app scan
            val scannedApps = appScanner.scanInstalledApps()
            val riskyApps = scannedApps.filter { it.riskScore > 30 }.take(5)
            
            val score = calculateScore(isRooted, isUsbDebug, isUnknownSources, riskyApps.size)
            
            val status = SecurityStatus(
                isRooted = isRooted,
                isUsbDebuggingEnabled = isUsbDebug,
                isUnknownSourcesEnabled = isUnknownSources,
                securityPatchLevel = patchLevel,
                riskScore = score
            )
            
            _uiState.value = DashboardUiState.Success(status, riskyApps)
        }
    }

    private fun calculateScore(isRooted: Boolean, isUsb: Boolean, isUnknown: Boolean, riskyAppCount: Int): Int {
        var score = 100
        if (isRooted) score -= 40
        if (isUsb) score -= 20
        if (isUnknown) score -= 10
        score -= (riskyAppCount * 5)
        return score.coerceAtLeast(0)
    }
}

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(
        val status: SecurityStatus,
        val riskyApps: List<ScannedApp> = emptyList()
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}
