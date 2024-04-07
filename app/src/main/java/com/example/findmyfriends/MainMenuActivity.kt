package com.example.findmyfriends

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.example.findmyfriends.ui.composables.MainNav
import com.example.findmyfriends.viewmodel.FirebaseDataViewModel
import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import com.example.findmyfriends.viewmodel.LocationViewModel
import com.google.android.gms.location.LocationServices

class MainMenuActivity : ComponentActivity() {
    private val fbvm: FirebaseDataViewModel by viewModels {FirebaseDataViewModel.Factory}
    private val lvm: LocationViewModel by viewModels {LocationViewModel.Factory  }
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted.
                Log.d("TAGPERMISSION", "Granted Precise")

            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted.
                Log.d("TAGPERMISSION", "Granted Approx")

            } else -> {
            // No location access granted.
            Log.d("TAGPERMISSION", "Denied")
            }
        }
    }

    private fun requestLocationPermissions(){
        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        requestLocationPermissions()
        setContent {
            MainNav(fbvm,lvm)
        }
    }
    override fun onResume() {
        super.onResume()
        if (checkGPSPermissions()){
            lvm.setLocalizationOn(this)
        }
    }
    override fun onPause() {
        super.onPause()
        lvm.setLocalizationOff()
    }
    fun checkGPSPermissions() : Boolean{
        return ((checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED))
    }

}