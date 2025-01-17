package com.raju.realtimelocation.main.domain

import com.raju.realtimelocation.core.domain.util.NetworkError
import com.raju.realtimelocation.core.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface MainDataSource {
    suspend fun pushCurrentLocation(data: CurrentLocation): Result<String, NetworkError>

    suspend fun receiveLocationUpdates(): Flow<CurrentLocation>

    suspend fun closeWebSocket()
}