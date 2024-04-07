package com.example.findmyfriends

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.findmyfriends.data.FirebaseServices
import com.example.findmyfriends.ui.composables.LoginNav

class LoginActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (FirebaseServices.currentUser != null) {
            Toast.makeText(this, "Hello, ${FirebaseServices.currentUser!!.displayName}", Toast.LENGTH_LONG).show()
            val intent = Intent(this, MainMenuActivity::class.java)
            this.startActivity(intent)
            this!!.finish()
        }
        else{
            setContent {
                LoginNav()
            }
        }
    }
    public override fun onStart() {
        super.onStart()
    }
}
