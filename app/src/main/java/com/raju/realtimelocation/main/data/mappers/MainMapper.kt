package com.raju.realtimelocation.main.data.mappers

import com.raju.realtimelocation.main.data.networking.dto.CurrentLocationDto
import com.raju.realtimelocation.main.domain.CurrentLocation

fun CurrentLocation.toMap(): CurrentLocationDto {
    return CurrentLocationDto(
        deviceId = deviceId,
        latitude = latitude,
        longitude = longitude
    )
}

fun CurrentLocationDto.toMap(): CurrentLocation {
    return CurrentLocation(
        deviceId = deviceId,
        latitude = latitude,
        longitude = longitude
    )
}
