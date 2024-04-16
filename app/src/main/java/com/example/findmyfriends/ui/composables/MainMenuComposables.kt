package com.example.findmyfriends.ui.composables

import android.Manifest
import android.R
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.findmyfriends.CameraActivity
import com.example.findmyfriends.LoginActivity
import com.example.findmyfriends.MapsActivity
import com.example.findmyfriends.data.FirebaseServices
import com.example.findmyfriends.data.model.FMFUser
import com.example.findmyfriends.ui.theme.YellowColor
import com.example.findmyfriends.viewmodel.FirebaseDataViewModel
import com.example.findmyfriends.viewmodel.LocationViewModel
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.Integer.min
import java.util.EnumMap
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


var isGpsEnabled = false

var currentUser = FirebaseServices.currentUser?.displayName?.let { FirebaseServices.currentUser!!.email?.let { it1 ->
    FMFUser(it,
        it1, FirebaseServices.currentUser!!.uid)
} }


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, fbvm: FirebaseDataViewModel, lvm: LocationViewModel) {
    val context = LocalContext.current
    val activity = LocalContext.current as? Activity
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    isGpsEnabled = checkGPSPermissions(activity!!)
    fbvm.loadGroups()
    var groups = fbvm.groups.observeAsState()



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
                        "My Groups",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                    )
                    )
                },
                navigationIcon = {
                    if (navController.previousBackStackEntry != null){
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Localized description"
                            )
                        }
                    }
                    else {
                        OutlinedButton(onClick = {
                            makeToastText(context, "Logging out..",false)
                            logout(context,activity)
                        }){
                            Text("Logout")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("main_screen")
                        {
                            popUpTo("main_screen") { inclusive = true }
                        }
//                        val intent = Intent(context, CameraActivity::class.java)
//                        context.startActivity(intent)
                    }) {
                        Icon(
//                            imageVector = Icons.Filled.CameraAlt,
//                            contentDescription = "Use Camera",
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh",
                            modifier = Modifier.scale(0.85f)
                        )
                    }
                    IconButton(onClick = {
                        navController.navigate("profile_page")

                    }) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Profile Page",
                            modifier = Modifier.scale(0.85f)

                        )
                    }


                },
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = {
            BottomAppBar(modifier = Modifier.heightIn(0.dp, 40.dp),
                actions = {
                    OutlinedButton(modifier = Modifier
                        .fillMaxWidth(0.5f) //Fill 0.5float (50%) of max width i.e. half the screen's width
                        .padding(0.dp)
                        .height(35.dp),
                        onClick = {

                            val builder = AlertDialog.Builder(context)
                            builder.setCancelable(false)

                            builder.setTitle ("Create a New Group")
                            val newGroupName = EditText(context)
                            newGroupName.hint = "Group Name"
                            newGroupName.setSingleLine()
                            newGroupName.inputType = InputType.TYPE_CLASS_TEXT
                            builder.setView(newGroupName)
                            builder.setPositiveButton(
                                "Confirm"
                            ) { _, _ ->
                                fbvm.addGroup(newGroupName.text.toString())
                                makeToastText(context, "Creating Group..", false)

                            }
                            builder.setNegativeButton(
                                R.string.cancel
                            ) { dialog, which -> }

                            val dialog = builder.create()
                            dialog.show()

                        }){
                        Text("Create a new Group")
                    }
                    OutlinedButton(modifier = Modifier
                        .fillMaxWidth(1f) //Fill 1.0 float (100%) of the remaining half screen
                        .padding(0.dp),
                        onClick = {
                            val builder = AlertDialog.Builder(context)
                            builder.setCancelable(false)
                            builder.setTitle("Join an existing group")
                            val joinedGroupID = EditText(context)
                            joinedGroupID.hint = "Group ID"
                            joinedGroupID.setSingleLine()
                            builder.setView(joinedGroupID)
                            builder.setPositiveButton(
                                "Confirm"
                            ) { _, _ ->
                                fbvm.joinGroup(joinedGroupID.text.toString())
                                navController.navigate("main_screen")
                                {
                                    popUpTo("main_screen") { inclusive = true }
                                }
                            }
                            builder.setNegativeButton(
                                R.string.cancel
                            ) { dialog, which -> }
                            builder.setNeutralButton("Scan QR"){
                                    _, _ ->
                                val intent = Intent(context, CameraActivity::class.java)
                                context.startActivity(intent)
                            }
                            val dialog = builder.create()
                            dialog.show()

                        }){
                        Text("Join Group")
                    }
                }
            )

        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding),
//        verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,

            ) {
            Spacer(modifier = Modifier.height(30.dp))
            Column {
                groups.value?.forEach { (groupID,groupp) ->
                    val group = groups.value!![groupID]
                    if (group != null) {
                        Card(
                            elevation = 10.dp,
                            border = BorderStroke(1.dp, Color.Black),
                            modifier = Modifier
                                .padding(10.dp, 0.dp, 10.dp, 10.dp)
                                .fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(10.dp, 10.dp, 10.dp, 10.dp)
                                    .fillMaxWidth()
                                    .clickable { navController.navigate("group_detail/${groupID}") },
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = "${group.name}",
                                    // modifier = Modifier.align(Alignment.Start)
                                )

                                if (currentUser != null) {
                                    if (group.adminId == currentUser!!.id) {
                                        Text(
                                            //modifier = Modifier.align(Alignment.CenterEnd),
                                            text = "Group Admin",
                                            // modifier = Modifier.align(Alignment.Start)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

            }

        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePage(navController: NavController, fbvm: FirebaseDataViewModel){
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val context = LocalContext.current
    val activity = LocalContext.current as? Activity

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
                        "My Profile",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,

            ) {

                val username = remember { mutableStateOf(TextFieldValue(currentUser?.username ?: "NameNotFound")) }
                val email = remember { mutableStateOf(TextFieldValue(currentUser?.email ?: "NameNotFound")) }

                val currentPassword = remember { mutableStateOf(TextFieldValue()) }
                val newPassword = remember { mutableStateOf(TextFieldValue()) }
                val newPasswordConfirm = remember { mutableStateOf(TextFieldValue()) }
                var passwordVisible by rememberSaveable { mutableStateOf(false) }


                //Initialization
                Spacer(modifier = Modifier.height(20.dp))
                TextField(
                    label = { Text(text = "Email") },
                    value = email.value,
                    onValueChange = { email.value = it })
                Spacer(modifier = Modifier.height(20.dp))
                TextField(
                    label = { Text(text = "Username") },
                    value = username.value,
                    onValueChange = { username.value = it })

            Spacer(modifier = Modifier.height(10.dp))
            TextField(
                label = { Text(text = "Current Password") },
                value = currentPassword.value,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff

                    // Please provide localized description for accessibility services
                    val description = if (passwordVisible) "Hide password" else "Show password"

                    IconButton(onClick = {passwordVisible = !passwordVisible}){
                        Icon(imageVector  = image, description)
                    }
                },
                onValueChange = { currentPassword.value = it })
                Spacer(modifier = Modifier.height(10.dp))
                TextField(
                    label = { Text(text = "New Password") },
                    value = newPassword.value,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image = if (passwordVisible)
                            Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff

                        // Please provide localized description for accessibility services
                        val description = if (passwordVisible) "Hide password" else "Show password"

                        IconButton(onClick = {passwordVisible = !passwordVisible}){
                            Icon(imageVector  = image, description)
                        }
                    },
                    onValueChange = { newPassword.value = it })
                Spacer(modifier = Modifier.height(10.dp))
                TextField(
                    label = { Text(text = "Confirm New Password") },
                    value = newPasswordConfirm.value,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image = if (passwordVisible)
                            Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff

                        // Please provide localized description for accessibility services
                        val description = if (passwordVisible) "Hide password" else "Show password"

                        IconButton(onClick = {passwordVisible = !passwordVisible}){
                            Icon(imageVector  = image, description)
                        }
                    },
                    onValueChange = { newPasswordConfirm.value = it })
                Spacer(modifier = Modifier.height(40.dp))
                Box(modifier = Modifier.padding(40.dp, 0.dp, 40.dp, 0.dp)) {
                    Button(
                        onClick = {
                            val emailInserted = email.value.text
                            val usernameInserted = username.value.text
                            val currentPasswordInserted = currentPassword.value.text
                            val newPasswordInserted = newPassword.value.text
                            val newPasswordConfirmInserted = newPasswordConfirm.value.text
                            var message = "Please insert at least username, email and current password."
                            val builder = AlertDialog.Builder(context)
                            builder.setCancelable(true)
                            if (emailInserted.isNotEmpty() && usernameInserted.isNotEmpty() && currentPasswordInserted.isNotEmpty() ){
                                if (newPasswordInserted.isEmpty() || (newPasswordConfirmInserted.isNotEmpty() && newPasswordInserted==newPasswordConfirmInserted)) {
                                    var coroutineScope = CoroutineScope(Dispatchers.Default)
                                    var message = "NOT STARTED"
                                    coroutineScope.launch {
                                        runBlocking {
                                            launch {
                                                message= fbvm.updateUserProfile(emailInserted,usernameInserted,currentPasswordInserted,newPasswordInserted)

                                            }
                                        }.join()
                                        currentUser = FirebaseServices.currentUser?.displayName?.let { FirebaseServices.currentUser!!.email?.let { it1 ->
                                            FMFUser(it,
                                                it1, FirebaseServices.currentUser!!.uid)
                                        } }
                                        activity?.runOnUiThread(Runnable(){
                                            run(){
                                                builder.setMessage(message)
                                                builder.setPositiveButton(
                                                    "Close Message"
                                                ) { dialog, which ->
                                                }
                                                val dialog = builder.create()
                                                dialog.show()                                            }
                                        })

                                    }
                                }
                                else{
                                    message = "ERROR: The two new passwords must match"
                                    builder.setMessage(message)
                                    builder.setPositiveButton(
                                        "Close Message"
                                    ) { dialog, which ->
                                    }
                                    val dialog = builder.create()
                                    dialog.show()
                                }
                            }
                            else{
                                builder.setMessage(message)
                                builder.setPositiveButton(
                                    "Close Message"
                                ) { dialog, which ->
                                }
                                val dialog = builder.create()
                                dialog.show()
                            }


                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(YellowColor)
                    ) {
                        Text(text = "Confirm Changes", fontSize = 20.sp)
                    }
                }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailPage(navController: NavController, fmfGroupId: String, fbvm: FirebaseDataViewModel, lvm: LocationViewModel) {//vm: MapViewModel, fm: FirebaseViewModel){
    fbvm.currentGroupId=fmfGroupId
    lvm.setCurrentGroup(fmfGroupId)
    val context = LocalContext.current
    val activity = LocalContext.current as? Activity
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val fmfGroup = fbvm.groups.value?.get(fmfGroupId)
    fmfGroup?.name?.let { Log.d("GROUPTAG", "name: $it, id: $fmfGroupId") }
    fbvm.loadGroupUsers(fmfGroupId)
    var users  = fbvm.users.observeAsState()

    var coroutineScope = CoroutineScope(Dispatchers.Default)
    var message = "NOT STARTED"

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    var groupName: String =  "Group Name not found"
                    if (fmfGroup?.name != null){
                        groupName = fmfGroup.name!!
                    }
                    Text(
                        groupName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
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
        },
        bottomBar = {
            BottomAppBar(modifier = Modifier.heightIn(0.dp, 40.dp),
                actions = {
                    OutlinedButton(modifier = Modifier
                        .fillMaxWidth()
                        .padding(2.dp)
                        .height(35.dp),
                        onClick = {
                            val intent = Intent(context, MapsActivity::class.java)
                            intent.putExtra("groupID",fmfGroupId)
                            context.startActivity(intent)
                        }) {
                        Text("Find My Friends!")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding),
//        verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,

            ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)){
                val qrCodeBitMap = generateQRCode(fmfGroupId)
                if (qrCodeBitMap != null){
                    Image(qrCodeBitMap!!.asImageBitmap(),
                        contentDescription =  fmfGroupId,
                        modifier = Modifier.fillMaxWidth(),
                       // Alignment = Alignment.Center
                        )
                }
            }
            Row(Modifier.fillMaxWidth()){
                OutlinedButton(modifier = Modifier
                    .fillMaxWidth(0.5f) //Fill 0.5float (50%) of max width i.e. half the screen's width
                    .padding(0.dp)
                    .height(35.dp),
                    onClick = {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            if (fmfGroup != null) {
                                putExtra(Intent.EXTRA_TEXT, "Join my group \" ${fmfGroup.name} \" on Find My Friends, use the ID: $fmfGroupId")
                            }
                            type = "text/plain"
                        }

                        val shareIntent = Intent.createChooser(sendIntent, null)
                        startActivity(context, shareIntent, null)
                    }){
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        horizontalArrangement = Arrangement.SpaceAround,
                    ){
                        Text("Share Group")
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Sharing the id of the group"
                        )
                    }

                }
                OutlinedButton(modifier = Modifier
                    .fillMaxWidth(1f) //Fill 1.0 float (100%) of the remaining half screen
                    .padding(0.dp)
                    .height(35.dp),
                    onClick = {
                        val builder = AlertDialog.Builder(context)
                        builder.setCancelable(true)
                        builder.setTitle("Leave Group")
                        var message="Do you really want to leave this group?"
                        if (fmfGroup?.adminId == currentUser?.id){
                            message += "\nWhen an admin leaves, the group is deleted!"
                        }
                        builder.setMessage(message)
                        builder.setPositiveButton(
                            "Confirm"
                        ) { dialog, which ->
                            var coroutineScope = CoroutineScope(Dispatchers.Default)
                            coroutineScope.launch {
                                runBlocking {
                                    launch {
                                        fbvm.leaveGroup(fmfGroupId)
                                    }
                                }
                            }
                            makeToastText(context,"You left the group", true)
                            navController.navigate("main_screen")
                            {
                                popUpTo("main_screen") { inclusive = true }

                        }}
                        builder.setNegativeButton(
                            R.string.cancel
                        ) { dialog, which -> }

                        val dialog = builder.create()
                        dialog.show()
                    }){
                    Text("Leave Group")
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
            Column {
                users.value?.forEach {(id,user)->
                    val user = users.value!![id]
                    var color = Color.White
                    if (currentUser != null) {
                        if (id== currentUser!!.id){
                            color = Color(81, 252, 169)
                        }
                    }
                    Card(elevation = 10.dp,
                        border = BorderStroke(1.dp, Color.Black),
                        modifier = Modifier
                            .padding(10.dp, 0.dp, 10.dp, 10.dp)
                            .fillMaxWidth(),
                        backgroundColor = color
                    ) {

                        Row(
                            modifier = Modifier
                                .padding(10.dp, 10.dp, 10.dp, 10.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            if (user != null) {
                                if (currentUser!!.id == user.id){
                                    Text(
                                        text = "You"
                                        // modifier = Modifier.align(Alignment.Start)
                                    )
                                } else {
                                    Text(
                                        text = user.username
                                        // modifier = Modifier.align(Alignment.Start)
                                    )
                                }
                                if (user.id == fmfGroup?.adminId) {
                                    Text(
//modifier = Modifier.align(Alignment.CenterEnd),
                                        text = "Group Admin",
                                        // modifier = Modifier.align(Alignment.Start)
                                    )
                                }
                            }



                        }


                    }
                }

            }

        }
    }


}
fun generateQRCode(data: String): Bitmap? {
    val bitMatrix: BitMatrix = try {
        val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
        hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
        MultiFormatWriter().encode(
            data,
            BarcodeFormat.QR_CODE,
            500,
            500,
            //qrCodeWidthPixels,
            //qrCodeWidthPixels,
            hints
        )
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }

    val qrCodeWidth = bitMatrix.width
    val qrCodeHeight = bitMatrix.height
    val pixels = IntArray(qrCodeWidth * qrCodeHeight)

    for (y in 0 until qrCodeHeight) {
        val offset = y * qrCodeWidth
        for (x in 0 until qrCodeWidth) {
            pixels[offset + x] = if (bitMatrix[x, y]) {
                Color.Black.toArgb()//resources.getColor(R.color.secondary, theme) // QR code color
            } else {
                Color.White.toArgb()//resources.getColor(R.color.primary, theme) // Background color
            }
        }
    }

    val bitmap = Bitmap.createBitmap(qrCodeWidth, qrCodeHeight, Bitmap.Config.RGB_565)
    bitmap.setPixels(pixels, 0, qrCodeWidth, 0, 0, qrCodeWidth, qrCodeHeight)


    return bitmap
}


@SuppressLint("UnsafeOptInUsageError")
@Composable
fun CameraScreen(executor: Executor,
                 fbvm: FirebaseDataViewModel,
                 onError: (ImageCaptureException) -> Unit
){
    var qrCodeScanned by remember {
        mutableStateOf(false)
    }
    val lensFacing = CameraSelector.LENS_FACING_BACK

    val context = LocalContext.current
    val activity = LocalContext.current as Activity

    val lifecycleOwner = LocalLifecycleOwner.current
    val configuration = LocalConfiguration.current

    val screenWidth = configuration.screenWidthDp * configuration.densityDpi / 160
    val screenHeight = configuration.screenHeightDp * configuration.densityDpi / 160

    val preview = Preview.Builder().build()
    val previewView = remember {
        PreviewView(context)
    }
    val imageAnalyzer = ImageAnalysis.Builder()
        .setTargetResolution(android.util.Size(screenWidth, screenHeight))
        .build()
    imageAnalyzer.setAnalyzer(executor){
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        val scanner = BarcodeScanning.getClient(options)

        val mediaImage = it.image
        if (mediaImage != null) {

            val image = InputImage.fromMediaImage(mediaImage, it.imageInfo.rotationDegrees)
            scanner.process(image).addOnSuccessListener {
                    barcodes ->
                if (barcodes.isNotEmpty()){
                    for (barcode in barcodes) {
                        if (!qrCodeScanned){
                            qrCodeScanned = true
                            fbvm.joinGroup(barcode.rawValue.toString())
                            makeToastText(context,"QR Scanned, joining group...",false)
                            activity.finish()
                        }
                    }
                }

                it.close()
            }
                .addOnFailureListener(){
                    qrCodeScanned = false
                    makeToastText(context,"Error while scanning the QR Code",false)

                    Log.e("TAG FailureDetector", it.toString())
                }

        }
        else {
            Log.i("TAG CAMERA_IMAGEANALYZER", "Null mediaImage")
        }
    }


    val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

    LaunchedEffect(lensFacing ){
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(lifecycleOwner,cameraSelector,preview, imageAnalyzer)

        preview.setSurfaceProvider(previewView.surfaceProvider)
    }

    Box( contentAlignment = Alignment.BottomCenter,
        modifier = Modifier.fillMaxSize()){
        val width = LocalConfiguration.current.screenWidthDp * ( LocalConfiguration.current.densityDpi/160)
        val height =  LocalConfiguration.current.screenHeightDp * ( LocalConfiguration.current.densityDpi/160)
        val squareSize = min(width,height) * (1F/2F)
        AndroidView({ previewView }, modifier = Modifier.fillMaxSize())
        Canvas(modifier = Modifier.fillMaxSize()){

            drawRect(color = Green,
                topLeft = Offset(x = (width-squareSize)/2F, y = (height-squareSize)/2F),
                size = Size(squareSize,squareSize), style = Stroke(8F))
        }
    }
}

@Composable
fun MissingCameraPermissionDialog(){

    val activity = LocalContext.current as Activity

    AlertDialog(
        modifier = Modifier.clip(RoundedCornerShape(20.dp)),
        backgroundColor = Color.White,
        onDismissRequest = {},
        title = {
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(),
            ){
                androidx.compose.material.Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Camera Permission not granted",
                    textAlign = TextAlign.Center
                )
            }
        },
        buttons = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        activity.finish()
                    }) {
                    androidx.compose.material.Text(text = "Return to main screen")
                }
            }
        }

    )


}
private suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine{
    ProcessCameraProvider.getInstance(this).also { cameraProvider ->
        cameraProvider.addListener({
            it.resume(cameraProvider.get())
        }, ContextCompat.getMainExecutor(this))
    }

}



@Composable
fun MainNav(fbvm: FirebaseDataViewModel, lvm: LocationViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController,
        startDestination = "main_screen"
    ) {

        composable("main_screen") {
            MainScreen(navController = navController,fbvm = fbvm, lvm = lvm)
        }

        composable("profile_page") {
            ProfilePage(navController = navController,fbvm = fbvm)
        }

        composable("group_detail/{group_id}"){
                navBackStackEntry ->
            val groupId = navBackStackEntry.arguments?.getString("group_id")
            groupId?.let {
                GroupDetailPage(navController = navController, it, fbvm = fbvm, lvm = lvm)
            }
        }
    }
}

fun logout(context: Context ,activity: Activity?){

    FirebaseServices.logout()
    val intent = Intent(context, LoginActivity::class.java)
    context.startActivity(intent)
    activity!!.finish()
}

fun makeToastText(context: Context , text:String,long: Boolean){
    if (long){
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }
    else{
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }
}

fun checkGPSPermissions(activity:Activity) : Boolean{
    return ((activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
            (activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED))
}


