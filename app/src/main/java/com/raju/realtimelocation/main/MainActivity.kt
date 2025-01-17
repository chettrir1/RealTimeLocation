package com.raju.realtimelocation.main

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.raju.realtimelocation.main.domain.CurrentLocation
import com.raju.realtimelocation.main.presentation.MainAction
import com.raju.realtimelocation.main.presentation.MainState
import com.raju.realtimelocation.ui.theme.RealTimeLocationTheme
import com.raju.realtimelocation.utils.getOrCreateDeviceId
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModel()
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            startLocationUpdates()
        } else {
            println("Permissions not granted")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        setContent {
            RealTimeLocationTheme {
                val state: MainState by viewModel.state.collectAsStateWithLifecycle()

                Scaffold(modifier = Modifier.Companion.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Device Id: ${state.receivedLocation.deviceId}")
                        Text(text = "Device Id: ${state.receivedLocation.latitude}")
                        Text(text = "Device Id: ${state.receivedLocation.longitude}")
                    }
                }
            }
        }
    }

    fun updateLocation(location: Location) {
        println("Location update: ${location.latitude}, ${location.longitude}")
        val data = CurrentLocation(
            deviceId = getOrCreateDeviceId(this@MainActivity),
            latitude = "${location.latitude}",
            longitude = "${location.longitude}"
        )
        viewModel.onAction(MainAction.OnLocationFetched(data))
    }

    private fun startLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val hasFineLocationPermission = ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocationPermission = ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFineLocationPermission && hasCoarseLocationPermission) {
            try {
                val locationRequest =
                    LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L).build()

                locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        super.onLocationResult(locationResult)
                        locationResult.let {
                            for (location in it.locations) {
                                updateLocation(location)
                            }
                        }
                    }
                }

                // Request location updates
                fusedLocationClient.requestLocationUpdates(
                    locationRequest, locationCallback, mainLooper
                )
            } catch (e: Exception) {
                Log.d("Location Error", "Error starting location updates", e)
            }
        } else {
            println("Location permissions are not granted")
        }
    }

    override fun onDestroy() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        viewModel.onAction(MainAction.OnWebSocketClosed)
        super.onDestroy()
    }
}