package com.raju.realtimelocation.main.presentation

import androidx.compose.runtime.Immutable

@Immutable
data class MainState(
    val isLoading: Boolean = false,
    val isLocationSent: Boolean = false
)