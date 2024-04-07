package com.example.findmyfriends.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.findmyfriends.R


@Composable
fun LogoText(scale: Float, fullScreen: Boolean = false, mod: Modifier= Modifier ){

    var modifier = Modifier.scale(scale).then(mod)


    if (fullScreen){
     modifier = Modifier.fillMaxSize()
    }
    Box(contentAlignment = Alignment.Center,
        modifier =  modifier) {

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Logo",
                modifier = Modifier
                    .height((scale*100).dp),
                contentScale = ContentScale.Crop

            )
            Text(
                "Find My Friends",
                style = TextStyle(
                    fontSize = (scale*50).sp,
                    fontWeight = FontWeight.Bold
                ),
            )
        }
    }
}