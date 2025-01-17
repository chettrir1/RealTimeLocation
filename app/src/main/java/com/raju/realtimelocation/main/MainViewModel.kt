package com.raju.realtimelocation.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raju.realtimelocation.core.domain.util.onError
import com.raju.realtimelocation.core.domain.util.onSuccess
import com.raju.realtimelocation.main.domain.CurrentLocation
import com.raju.realtimelocation.main.domain.MainDataSource
import com.raju.realtimelocation.main.presentation.MainAction
import com.raju.realtimelocation.main.presentation.MainEvent
import com.raju.realtimelocation.main.presentation.MainState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(private val mainDataSource: MainDataSource) : ViewModel() {
    private var _state = MutableStateFlow(MainState())
    val state = _state
        .onStart { receiveLocationUpdates() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            MainState()
        )

    private val _events = Channel<MainEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: MainAction) {
        when (action) {
            is MainAction.OnLocationFetched -> {
                sendLocation(action.currentLocation)
            }

            MainAction.OnWebSocketClosed -> {
                closeWebSocket()
            }
        }
    }

    private fun sendLocation(currentLocation: CurrentLocation) {
        viewModelScope.launch {
            _state.update {
                it.copy(isLoading = true)
            }
            mainDataSource
                .pushCurrentLocation(currentLocation)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isLocationSent = true
                        )
                    }
                }
                .onError { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isLocationSent = false
                        )
                    }
                    _events.send(MainEvent.Error(error))
                }
        }
    }

    private fun receiveLocationUpdates() {
        viewModelScope.launch {
            mainDataSource.receiveLocationUpdates()
                .catch { e ->
                    Log.e("MainViewModel", "Error receiving location updates: ${e.message}")
                }
                .collect { location ->
                    _state.update {
                        it.copy(
                            receivedLocation = location
                        )
                    }
                }
        }
    }

    private fun closeWebSocket() {
        viewModelScope.launch {
            mainDataSource.closeWebSocket()
        }
    }
}