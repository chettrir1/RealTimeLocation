package com.raju.realtimelocation.main.presentation

import com.raju.realtimelocation.core.domain.util.NetworkError

sealed interface MainEvent {
    data class Error(val error: NetworkError) : MainEvent
}