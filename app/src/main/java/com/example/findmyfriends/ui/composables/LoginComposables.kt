package com.example.findmyfriends.ui.composables


import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.findmyfriends.MainMenuActivity
import com.example.findmyfriends.data.FirebaseServices
import com.example.findmyfriends.data.model.FMFUser
import com.example.findmyfriends.ui.theme.YellowColor
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Composable
fun LoginNav(){
    val navController = rememberNavController()
    NavHost(navController = navController,
        startDestination = "splash_screen"
    ) {

        composable("splash_screen") {
            SplashScreen(navController = navController)
        }

        composable("login") {
            LoginPage(navController = navController)
        }

        composable("signup"){
            RegistrationPage(navController = navController)
        }

    }
}

@Composable
fun SplashScreen(navController: NavController){
    val context = LocalContext.current
    val activity = LocalContext.current as? Activity
    val scale = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 0.7f,
            // tween Animation
            animationSpec = tween(
                durationMillis = 800,
                easing = {
                    OvershootInterpolator(4f).getInterpolation(it)
                })
        )
        // Customize the delay time

        delay(800L)

            navController.navigate("login")
            {
                popUpTo("splash_screen") { inclusive = true }
           }


    }

    LogoText(scale = scale.value,fullScreen = true)
}

@Composable
fun LoginPage(navController: NavController){
    val context = LocalContext.current
    val activity = LocalContext.current as? Activity

    Column(
        modifier = Modifier.fillMaxSize(),
//        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,

        ){
        val scale = remember { Animatable(0f) }
        val email = remember { mutableStateOf(TextFieldValue()) }
        val password = remember { mutableStateOf(TextFieldValue()) }
        var passwordVisible by rememberSaveable { mutableStateOf(false) }

        //Logo in the top of the page
        LogoText(scale = 0.65f,false)
        Text(
            "Log into your account",
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            ),
//                modifier = Modifier.scale(scale)
        )
        Spacer(modifier = Modifier.height(20.dp))
        TextField(
            label = { Text(text = "Email") },
            value = email.value,
            onValueChange = { email.value = it })

        Spacer(modifier = Modifier.height(10.dp))
        TextField(
            label = { Text(text = "Password") },
            value = password.value,
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
            onValueChange = { password.value = it })

        Spacer(modifier = Modifier.height(50.dp))
        Box(modifier = Modifier.padding(40.dp, 0.dp, 40.dp, 0.dp)) {
            Button(
                onClick = {
                    val emailInserted = email.value.text
                    val passwordInserted = password.value.text

                    if (emailInserted.isNotEmpty() && passwordInserted.isNotEmpty()){
                        var coroutineScope= CoroutineScope(Dispatchers.Default)
                        var message = "NOT STARTED"
                        coroutineScope.launch {
                            runBlocking {

                                launch {
                                    message = FirebaseServices.login(emailInserted,passwordInserted)

                                }

                            }.join()
                            if (message.isEmpty()){
                                val intent = Intent(context, MainMenuActivity::class.java)
                                context.startActivity(intent)
                                activity!!.finish()
                            }
                            else{
                                activity?.runOnUiThread(Runnable(){
                                     run(){
                                         Toast.makeText(context,message, Toast.LENGTH_SHORT).show()
                                     }
                                })
                                Log.d("LOGINTAGSUCCESSFALSE","LOGINTAG SUCCESS FALSE")
                            }
                        }
                        Log.d("LOGINTAGSUCCESSEND","LOGINTAG SUCCESS END")

                    }else{
                        Toast.makeText(context,"Please insert both email and password", Toast.LENGTH_SHORT).show()
                    }

                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(YellowColor)
            ) {
                Text(text = "Login", fontSize = 20.sp)
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()){
        ClickableText(
            text = AnnotatedString("Don't have an account? Sign up here"),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(20.dp),
            onClick = {
                navController.navigate("signup")
                {
                    popUpTo("login") { inclusive = false }
                }
            },
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = FontFamily.Default,
                textDecoration = TextDecoration.Underline
            )
        )
    }

}

@Composable
fun RegistrationPage(navController: NavController){
    val context = LocalContext.current
    val activity = LocalContext.current as? Activity

    Column(
        modifier = Modifier.fillMaxSize(),
//        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,

        ){
        val scale = remember { Animatable(0f) }
        val username = remember { mutableStateOf(TextFieldValue()) }
        val email = remember { mutableStateOf(TextFieldValue()) }
        val password = remember { mutableStateOf(TextFieldValue()) }
        val passwordConfirm = remember { mutableStateOf(TextFieldValue()) }
        var passwordVisible by rememberSaveable { mutableStateOf(false) }

        //Logo in the top of the page
        LogoText(scale = 0.65f,false)
        Text(
            "Create a new account",
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            ),
//                modifier = Modifier.scale(scale)
        )
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
            label = { Text(text = "Password") },
            value = password.value,
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
            onValueChange = { password.value = it })
        Spacer(modifier = Modifier.height(10.dp))
        TextField(
            label = { Text(text = "Confirm Password") },
            value = passwordConfirm.value,
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
            onValueChange = { passwordConfirm.value = it })
        Spacer(modifier = Modifier.height(40.dp))
        Box(modifier = Modifier.padding(40.dp, 0.dp, 40.dp, 0.dp)) {
            Button(
                onClick = {
                    val emailInserted = email.value.text
                    val usernameInserted = username.value.text
                    val passwordInserted = password.value.text
                    val passwordConfirmInserted = passwordConfirm.value.text
                    if (emailInserted.isNotEmpty() && usernameInserted.isNotEmpty() && passwordInserted.isNotEmpty()&& passwordConfirmInserted.isNotEmpty()){

                        if(passwordConfirmInserted == passwordInserted){
                            FirebaseServices.auth.createUserWithEmailAndPassword(emailInserted, passwordInserted)
                                .addOnSuccessListener {


                                    //Log.i("TAG REGISTER", FirebaseRepository.getUser()!!.email.toString())

                                    FirebaseServices.auth.currentUser!!.updateProfile(userProfileChangeRequest {
                                        displayName = usernameInserted
                                    }).addOnSuccessListener {
                                        FirebaseServices.addUser(FMFUser(usernameInserted,emailInserted,
                                            FirebaseServices.auth.currentUser!!.uid))
                                        val intent = Intent(context, MainMenuActivity::class.java)
                                        context.startActivity(intent)
                                        activity!!.finish()
                                    }
                                        .addOnFailureListener {
                                            Toast.makeText(context, "ERROR: ${it.message!!.split(".")[0]}", Toast.LENGTH_LONG).show()
                                        }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "ERROR: ${it.message!!.split(".")[0]}", Toast.LENGTH_LONG).show()
                                }
                        }
                        else{
                            Toast.makeText(context, "The two passwords must match.", Toast.LENGTH_LONG).show()
                        }

                    }else{
                        Toast.makeText(context, "Please fill all the fields", Toast.LENGTH_LONG).show()}
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(YellowColor)
            ) {
                Text(text = "Register", fontSize = 20.sp)
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()){
        ClickableText(
            text = AnnotatedString("Already have an account? Login Here"),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(20.dp),
            onClick = {
                navController.navigate("login")
                {
                    popUpTo("signup") { inclusive = true }
                }
                      },
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = FontFamily.Default,
                textDecoration = TextDecoration.Underline
            )
        )
    }
}
