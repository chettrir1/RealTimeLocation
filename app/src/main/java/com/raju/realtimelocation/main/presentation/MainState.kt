package com.raju.realtimelocation.main.presentation

import androidx.compose.runtime.Immutable
import com.raju.realtimelocation.main.domain.CurrentLocation

@Immutable
data class MainState(
    val isLoading: Boolean = false,
    val isLocationSent: Boolean = false,
    val receivedLocation: CurrentLocation = CurrentLocation(
        deviceId = "",
        latitude = "0.0",
        longitude = "0.0"
    )
)