package com.raju.realtimelocation.main.presentation

import com.raju.realtimelocation.main.domain.CurrentLocation

sealed interface MainAction {
    data class OnLocationFetched(val currentLocation: CurrentLocation) : MainAction
    data object OnWebSocketClosed : MainAction
}