package com.khw.ciod

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.White)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.mainactivitybackgroundicon),
                    contentDescription = "",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 56.dp)
                        .size(240.dp, 400.dp)
                )

                Image(
                    painter = painterResource(id = R.drawable.mainactivitybackgroundicon2),
                    contentDescription = "",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 120.dp)
                        .size(176.dp, 240.dp)
                )
                val context = LocalContext.current

                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(bottom = 40.dp)
                ) {
                    Text(
                        text = "CIOD",
                        fontSize = 54.sp,
                        color = Color.Black,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "Closet Is Open Door",
                        fontSize = 24.sp,
                        color = Color(0xFFADADAD),
                        modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                Button(
                    onClick = {
                        context.startActivity(Intent(context, LoginActivity::class.java))
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(40.dp)
                        .size(280.dp, 50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black
                    )
                ) {
                    Text(text = "로그인", fontSize = 16.sp)
                }
                TextButton(onClick = { 
                    context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
                },
                    modifier = Modifier.align(Alignment.TopEnd)) {
                    Text(text = "@Licenses", fontSize = 8.sp, color = Color.Gray, textAlign = TextAlign.End)
                }
            }
        }
    }
}