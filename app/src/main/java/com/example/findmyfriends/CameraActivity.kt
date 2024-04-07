package com.example.findmyfriends

import android.content.pm.PackageManager
import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.example.findmyfriends.ui.composables.CameraScreen
import com.example.findmyfriends.ui.composables.MissingCameraPermissionDialog
import com.example.findmyfriends.viewmodel.FirebaseDataViewModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity  : ComponentActivity() {
    private val fbvm: FirebaseDataViewModel by viewModels { FirebaseDataViewModel.Factory}

    private lateinit var cameraExecutor: ExecutorService
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
        isGranted ->
        if (isGranted) {
            setContent{CameraScreen(
                executor = cameraExecutor,
                onError = { Log.e("TAG ERROR CAMERA ACTIVITY","Exception: ",it)},
                fbvm = fbvm
            )}
        }
        else {
            setContent{MissingCameraPermissionDialog()}
            Log.i("TAG NO_CAMERA_PERMISSION","No camera Permission")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?,) {
        super.onCreate(savedInstanceState)
        setContent {
            CameraScreen(
                executor = cameraExecutor,
                onError = { Log.e("TAG ERROR CAMERA ACTIVITY","Exception: ",it)},
                fbvm = fbvm

            )
        }
        requestCameraPermission()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
    private fun requestCameraPermission(){
        if (ContextCompat.checkSelfPermission(this,
            Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)  {
            Log.i("TAG", "Permission previously granted")
        }
        else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            Log.i("TAG", "Show camera permissions dialog")
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                setContent { MissingCameraPermissionDialog() }
            }
        }

    }
}