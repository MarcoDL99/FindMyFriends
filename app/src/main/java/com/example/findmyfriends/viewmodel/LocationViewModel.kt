package com.example.findmyfriends.viewmodel

import android.annotation.SuppressLint
import android.app.Activity
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.findmyfriends.data.model.DataUser
import com.example.findmyfriends.ui.composables.currentUser
import com.example.findmyfriends.utilities.ApiRetrofitHelper
import com.example.findmyfriends.utilities.LocationApi
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import com.google.gson.internal.LinkedTreeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocationViewModel : ViewModel() {

    companion object {
        val Factory = viewModelFactory {
            initializer {
                LocationViewModel()

            }
        }
    }
    private var currentGroup =""
    val lastUpdate: MutableLiveData<String> = MutableLiveData()
    var groupUsersData : MutableLiveData<MutableList<DataUser>> = MutableLiveData()
    private val serverApi = ApiRetrofitHelper.getInstance().create(LocationApi::class.java)
    private val _location: MutableLiveData<Location> = MutableLiveData<Location>()
    val location: LiveData<Location> = _location
    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,5000).build() //5000 = updates every 5 seconds
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var localizationOn = false
    private val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            _location.value = locationResult.lastLocation!!
            viewModelScope.launch(Dispatchers.IO) {
                val response = serverApi.updateUser(currentUser!!.id,location.value!!.latitude.toString(),  location.value!!.longitude.toString())
                Log.i("TAG LOCATION_UPDATES_RESPONSE_FROM_SERVER", response.code().toString() + " " + response.message().toString())
                lastUpdate.postValue(System.currentTimeMillis().toString())
                if (currentGroup.isNotEmpty()){
                    val groupUsersNewData = serverApi.getGroupUpdate(currentGroup).body() //LinkedTreeMap<String,LinkedTreeMap<String,Any>>
                    val groupUsersNewList = mutableListOf<DataUser>()
                    if (groupUsersNewData != null) {
                            for (user in groupUsersNewData.keys){
                                val newUser = DataUser((groupUsersNewData[user]?.get("latitude") as String).toDouble(),(groupUsersNewData[user]?.get("longitude") as String).toDouble(),user,(groupUsersNewData[user]?.get("timestamp") as String))
                                groupUsersNewList.add(newUser)


                            }
                       groupUsersData.postValue(groupUsersNewList)
                    }
                }
            }
        }
    }



    @SuppressLint("MissingPermission") // Permissions are already checked in activity code
    fun setLocalizationOn(activity: Activity){
        val newFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
        val client: SettingsClient = LocationServices.getSettingsClient(activity)
        val checkLocationEnabledTask: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        checkLocationEnabledTask.addOnSuccessListener {
            if(!localizationOn) {
                localizationOn = true
                fusedLocationClient = newFusedLocationClient
                fusedLocationClient!!.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            }
        }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException){
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        exception.startResolutionForResult(activity,
                            0)
                    } catch (e: Exception) {
                        // Ignore the error
                        Log.d("MAPSEXCEPTION", e.message.toString())
                    }
                }
            }

    }
    fun setLocalizationOff(){

        if(fusedLocationClient!=null){
            fusedLocationClient!!.removeLocationUpdates(locationCallback)
            localizationOn = false
        }
    }
    fun setCurrentGroup(groupId: String){
        currentGroup = groupId
    }
}
