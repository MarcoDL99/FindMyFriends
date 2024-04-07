package com.example.findmyfriends

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.example.findmyfriends.ui.composables.MapScreen
import com.example.findmyfriends.viewmodel.FirebaseDataViewModel
import com.example.findmyfriends.viewmodel.LocationViewModel

class MapsActivity : ComponentActivity()  {
    private val fbvm: FirebaseDataViewModel by viewModels { FirebaseDataViewModel.Factory}
    private val lvm: LocationViewModel by viewModels { LocationViewModel.Factory  }
    private var currentGroupID = ""
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
    override fun onCreate(savedInstanceState: Bundle?) {

        currentGroupID = intent.getStringExtra("groupID").toString()
        super.onCreate(savedInstanceState)
        requestLocationPermissions()
        Log.d("PERMISSIONMAP", checkGPSPermissions().toString())
        setContent {
            if (currentGroupID != null) {
                MapScreen(fbvm = fbvm, fmfGroupId = currentGroupID, lvm = lvm)
            }
        }
    }
    private fun requestLocationPermissions(){
        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION))
    }
    override fun onResume() {
        super.onResume()
        if (checkGPSPermissions()){
            lvm.setLocalizationOn(this)
        }
        else {
            requestLocationPermissions()
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