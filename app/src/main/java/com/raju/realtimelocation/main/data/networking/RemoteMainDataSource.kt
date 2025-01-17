package com.raju.realtimelocation.main.data.networking

import android.util.Log
import com.raju.realtimelocation.core.domain.util.NetworkError
import com.raju.realtimelocation.core.domain.util.Result
import com.raju.realtimelocation.main.data.mappers.toMap
import com.raju.realtimelocation.main.data.networking.dto.CurrentLocationDto
import com.raju.realtimelocation.main.domain.CurrentLocation
import com.raju.realtimelocation.main.domain.MainDataSource
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.url
import io.ktor.utils.io.InternalAPI
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.jsonObject

class RemoteMainDataSource(
    private val httpClient: HttpClient
) : MainDataSource {

    private val socketUrl = "ws://10.0.2.2:1000"

    //socket connection
    private var socket: WebSocketSession? = null

    //connect to the websocket server
    private suspend fun connectWebSocket() {
        if (socket == null) {
            socket = httpClient.webSocketSession {
                url(socketUrl)
            }
        }
    }

    @ExperimentalSerializationApi
    @InternalAPI
    override suspend fun pushCurrentLocation(data: CurrentLocation): Result<String, NetworkError> {
        try {

            connectWebSocket()

            val jsonLocation = Json.encodeToString(data.toMap())
            socket?.send(Frame.Text(jsonLocation))

            return Result.Success("Location sent successfully!")

        } catch (e: Exception) {
            Log.e("RemoteMainDataSource", "Error sending location via WebSocket: ${e.message}")
            return Result.Error(NetworkError.UNKNOWN)
        }
    }

    override suspend fun receiveLocationUpdates(): Flow<CurrentLocation> = flow {
        try {
            connectWebSocket()
            socket?.incoming?.consumeEach { frame ->
                when (frame) {
                    is Frame.Text -> {
                        val message = frame.readText()
                        val location = Json.decodeFromString<CurrentLocationDto>(message)
                        Log.e(
                            "RemoteMainDataSource",
                            "Location Fetched: ${location.latitude} ${location.longitude}"
                        )
                        emit(location.toMap())
                    }

                    is Frame.Binary -> {
                        val message = frame.data.decodeToString()
                        val location = Json.decodeFromString<CurrentLocationDto>(message)
                        Log.e(
                            "RemoteMainDataSource",
                            "Location Fetched: ${location.latitude} ${location.longitude}"
                        )
                        emit(location.toMap())
                    }

                    else -> {
                        Log.d("WebSocket", "Non-text frame received")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("RemoteMainDataSource", "Error receiving location updates: ${e.message}")
        }

    }

    override suspend fun closeWebSocket() {
        socket?.close()
    }

}