package com.raju.realtimelocation.main.domain

import com.raju.realtimelocation.core.domain.util.NetworkError
import com.raju.realtimelocation.core.domain.util.Result

interface MainDataSource {
    suspend fun pushCurrentLocation(data: CurrentLocation): Result<String, NetworkError>
    suspend fun closeWebSocket()
}