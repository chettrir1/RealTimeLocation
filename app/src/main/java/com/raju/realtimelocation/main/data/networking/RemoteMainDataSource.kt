package com.raju.realtimelocation.main.data.networking

import android.util.Log
import com.raju.realtimelocation.core.domain.util.NetworkError
import com.raju.realtimelocation.core.domain.util.Result
import com.raju.realtimelocation.main.data.mappers.toMap
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
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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

            socket?.incoming?.consumeEach { frame ->
                when (frame) {
                    is Frame.Text -> {
                        Log.d("WebSocket Response", frame.readText())
                    }

                    else -> {
                        Log.d("WebSocket Response", "Non-Text frame received")
                    }
                }
            }
            return Result.Success("Location sent successfully!")

        } catch (e: Exception) {
            Log.e("RemoteMainDataSource", "Error sending location via WebSocket: ${e.message}")
            return Result.Error(NetworkError.UNKNOWN)
        }
    }

    override suspend fun closeWebSocket() {
        socket?.close()
    }

}