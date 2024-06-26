package com.example.findmyfriends.ui.composables

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Observer
import com.example.findmyfriends.MainMenuActivity
import com.example.findmyfriends.R
import com.example.findmyfriends.data.model.DataUser
import com.example.findmyfriends.data.model.UserRenderer
import com.example.findmyfriends.viewmodel.FirebaseDataViewModel
import com.example.findmyfriends.viewmodel.LocationViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.compose.*
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(fmfGroupId: String, fbvm: FirebaseDataViewModel, lvm: LocationViewModel){
    if (fmfGroupId!= null){
        fbvm.loadGroup(fmfGroupId)
        fbvm.loadGroupUsers(fmfGroupId)
        lvm.setCurrentGroup(fmfGroupId)
    }
    var clusterManager by remember {
        mutableStateOf<ClusterManager<DataUser>?>(null)
    }
    val group = fbvm.currentGroup.observeAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val context = LocalContext.current
    val activity = LocalContext.current as? Activity
    val location by lvm.location.observeAsState()
    var initialZoomInDone = false


    val mapProperties by remember {
        mutableStateOf(MapProperties(isBuildingEnabled = true, isMyLocationEnabled = true))
    }
    val mapUiSettings by remember {
        mutableStateOf(MapUiSettings(compassEnabled = true, zoomControlsEnabled = false, rotationGesturesEnabled = false))
    }
    val cameraPositionState: CameraPositionState = rememberCameraPositionState{}
    val cameraTarget : LatLng = cameraPositionState.position.target
    var boundsSW by remember {
        mutableStateOf(LatLng(-90.0,-180.0))
    }
    var boundsNE by remember {
        mutableStateOf(LatLng(90.0, 180.0))
    }
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        if (group?.value?.name!= null) group?.value?.name!! else "Name not Found",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                        IconButton(onClick = {
                            val intent = Intent(context, MainMenuActivity::class.java)
                            context.startActivity(intent)
                            activity!!.finish()
                        }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Localized description"
                            )
                        }

                },
                actions = {
                },
                scrollBehavior = scrollBehavior,
            )
        }
    ) { innerPadding ->

        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            ){
            GoogleMap(
                modifier = Modifier
                    .fillMaxSize(),
                properties = mapProperties,
                uiSettings = mapUiSettings,
                cameraPositionState = cameraPositionState,

                ){
                mapProperties.latLngBoundsForCameraTarget?.javaClass?.let { Log.i("TAG Camera", it.name) }
                if (cameraPositionState.projection?.visibleRegion?.latLngBounds != null){
                    boundsSW = cameraPositionState.projection?.visibleRegion?.latLngBounds?.southwest!!
                    boundsNE = cameraPositionState.projection?.visibleRegion?.latLngBounds?.northeast!!
                }
                else{
                }

                if(!initialZoomInDone && location != null){
                    LaunchedEffect(key1 = true, block = {
                        val currPosition = LatLng(location!!.latitude, location!!.longitude)
                        cameraPositionState.animate(
                            CameraUpdateFactory.newCameraPosition(
                                CameraPosition.Builder().target(currPosition).zoom(17f).tilt(45f).build()
                            )
                        )
                        initialZoomInDone = true
                    })
                }
                if (clusterManager != null) {
                    for (marker in clusterManager!!.markerCollection.markers){
                        if (!checkLatLngWithinBounds(
                                marker.position.latitude,
                                marker.position.longitude,
                                boundsSW,
                                boundsNE
                            )
                        ) {
                        }
                    }
                }


                val lifecycle = LocalLifecycleOwner.current
                MapEffect{
                        map ->
                    if (clusterManager == null){
                        clusterManager = ClusterManager<DataUser>(context, map)
                    }
                    lvm.groupUsersData.observe(lifecycle, Observer { groupUsers ->
                        clusterManager!!.clearItems()
                        groupUsers.forEach{
                            if (it.title!= currentUser?.id){
                                val user = DataUser(it.lat,it.lng,fbvm.users.value?.get(it.title)!!.username,it.snippet)
                                clusterManager!!.addItem(user)

                            }
                        }
                        clusterManager!!.renderer = UserRenderer(context, map, clusterManager!!)
                        clusterManager!!.setOnClusterClickListener { cluster ->
                            val builder = LatLngBounds.Builder()
                            for(user in cluster.items){
                                builder.include(LatLng(user.lat, user.lng))
                            }
                            map.animateCamera(
                                CameraUpdateFactory.newCameraPosition(
                                    CameraPosition.Builder().target(builder.build().center).tilt(45f).zoom(16.5f).build()
                                )
                            )
                            true
                        }
                    })


                }
            }
            if (location == null){
                Log.i("TAG LOCATION NULL","")
            }
            else{
                OverlayGraphics(clusterManager,cameraTarget,boundsSW,
                    boundsNE, context)
            }

        }

    }
}


@Composable
fun OverlayGraphics(clusterManager:  ClusterManager<DataUser>?, cameraLatLng: LatLng, boundsSW: LatLng,
                    boundsNE: LatLng, context: Context,){
    val cameraTarget = LatLng(cameraLatLng.latitude,cameraLatLng.longitude)
    val dpWidth: Float = context.resources.displayMetrics.widthPixels / context.resources.displayMetrics.density
    val dpHeight: Float = context.resources.displayMetrics.heightPixels / context.resources.displayMetrics.density
    //Check lat vs dp density
    val arrowWidth = 100F
    val arrowHeight = 150F //Need to account for top bar height
    val maxOffsetX = dpWidth-arrowWidth
    val maxOffsetY = dpHeight -arrowHeight
    if (clusterManager?.markerCollection?.markers!! != null) {

        for (marker in clusterManager?.markerCollection?.markers!!) {
            Log.i("MARKERS",marker.toString())
            if (!checkLatLngWithinBounds(
                    marker.position.latitude,
                    marker.position.longitude,
                    boundsSW,
                    boundsNE
                )){
                val mapHorizontalDensity = (dpWidth) / (boundsNE.longitude-boundsSW.longitude)
                val mapVerticalDensity = (dpHeight-50) / (boundsNE.latitude-boundsSW.latitude)
                var offsetX: Dp
                var horizontalAngle = 0
                if (marker.position.longitude>= boundsNE.longitude){
                    offsetX = maxOffsetX.dp
                    horizontalAngle = 1
                } else if (marker.position.longitude<= boundsSW.longitude){
                    offsetX = 0.dp
                    horizontalAngle = -1

                } else {
                    offsetX = (maxOffsetX / 2).dp

                }
                var offsetY: Dp
                var verticalAngle = 0
                if (marker.position.latitude>= boundsNE.latitude){
                    offsetY = 0.dp
                    verticalAngle = 1
                } else if (marker.position.latitude<= boundsSW.latitude){
                    offsetY = maxOffsetY.dp
                    verticalAngle = -1
                } else {
                    offsetY = (maxOffsetY / 2).dp
                }
                var rotation: Float by remember  { mutableFloatStateOf(0f)
                }
                var angle = 0
                if (verticalAngle == 1){
                    if (horizontalAngle == 1){ //Marker is north east of user
                        angle = 45
                    }
                    else if (horizontalAngle == -1) { //Marker is north west of user
                        angle = -45
                    }
                    else { //Marker is north  of user
                        angle = 0
                        offsetX = (((marker.position.longitude - boundsSW.longitude) * mapHorizontalDensity )-50F).dp
//                        offsetY = if (tempOffsetY>=maxOffsetY) {maxOffsetY.dp}
//                        else if (tempOffsetY<0) {0.dp}
//                        else {tempOffsetY.dp}
                    }
                }
                else if (verticalAngle == -1){
                    if (horizontalAngle == 1){//Marker is south east of user
                        angle = 180-45
                    }
                    else if (horizontalAngle == -1) { //Marker is south west of user
                        angle = 180+45
                    }
                    else {          //Marker is south of user
                        angle = 180
                        offsetX = (((marker.position.longitude - boundsSW.longitude) * mapHorizontalDensity )-50F).dp
                    }
                }
                else{
                    offsetY = (((boundsNE.latitude - marker.position.latitude) * mapVerticalDensity )-150F).dp
//                    offsetX = if (tempOffsetX>=maxOffsetX) {maxOffsetX.dp}
//                    else if (tempOffsetX<0) {0.dp}
//                    else {tempOffsetX.dp}
                    if (horizontalAngle == 1){ //Marker is  east of user
                        angle = 90
                    }
                    else if (horizontalAngle == -1) { //Marker is west of user
                        angle = 270
                    }
                    else {          //Marker is within screen bounds and arrow won't show
                        angle = 180
                    }
                }
                rotation = angle.toFloat()
                Image(
                    painter = painterResource(id = R.drawable.arrow_upward),
                    contentDescription = cameraTarget.toString(),
                    modifier = Modifier
                        .size(100.dp, 100.dp)
                        .offset(offsetX, offsetY)
                        .rotate(rotation),
                )
}
            }
    }
}

@Composable
fun checkLatLngWithinBounds(latObj: Double,lngObj: Double,southWest: LatLng,northEast: LatLng): Boolean{
    return ((latObj in southWest.latitude..northEast.latitude) && (lngObj in southWest.longitude..northEast.longitude))
}
