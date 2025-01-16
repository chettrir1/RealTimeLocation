package com.raju.realtimelocation.core.data.networking

private const val BASE_URL = "http://10.0.2.2:1000/api/"
fun constructUrl(url: String): String {
    return when {
        url.contains(BASE_URL) -> url
        url.startsWith("/") -> BASE_URL + url.drop(1)
        else -> BASE_URL + url
    }
}