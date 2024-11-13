package org.codeslu.wifiextender.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    fun handleUiActions(vararg actions: MainUiAction) {
        viewModelScope.launch {
            actions.forEach { action ->
                when (action) {
                    is MainUiAction.OnSetLoading -> {
                        _uiState.update { it.copy(isLoading = action.value) }
                    }

                    is MainUiAction.OnUpdateConnectedFlag -> {
                        _uiState.update { it.copy(isConnected = action.value) }
                    }

                    is MainUiAction.OnUpdateHotspotFlag -> {
                        _uiState.update { it.copy(isHotspotStarted = action.value) }
                    }

                    is MainUiAction.OnUpdateKeepScreenOnFlag -> {
                        _uiState.update { it.copy(isKeepScreenOn = action.value) }
                    }

                    is MainUiAction.OnUpdateWPSFlag -> {
                        _uiState.update { it.copy(isWPSOn = action.value) }
                    }
                    is MainUiAction.OnSetConnectedDevices -> {
                        _uiState.update { it.copy(connectedDevices = action.connectedDevices) }
                    }
                    is MainUiAction.OnSetHotspotName -> {
                        _uiState.update { it.copy(hotspotName = action.newName) }
                    }
                    is MainUiAction.OnSetHotspotPassword -> {
                        _uiState.update { it.copy(hotspotPassword = action.newPassword) }
                    }
                    else -> Unit
                }
            }
        }
    }
}