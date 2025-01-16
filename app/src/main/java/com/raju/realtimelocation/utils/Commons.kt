package com.raju.realtimelocation.utils

import android.content.Context
import java.util.UUID

fun getOrCreateDeviceId(context: Context): String {
    val sharedPreferences = context.getSharedPreferences("Device_ID", Context.MODE_PRIVATE)
    var deviceId = sharedPreferences.getString("deviceId", null)
    if (deviceId == null) {
        deviceId = UUID.randomUUID().toString()
        sharedPreferences.edit().putString("deviceId", deviceId).apply()
    }
    return deviceId
}