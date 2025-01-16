package com.raju.realtimelocation.main.data.networking.dto

import kotlinx.serialization.Serializable

@Serializable
data class CurrentLocationDto(
    val deviceId: String,
    val latitude: String,
    val longitude: String
)
