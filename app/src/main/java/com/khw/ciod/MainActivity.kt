package com.khw.ciod

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            Column {

                val mContext = LocalContext.current

                Button(onClick = {
                    mContext.startActivity(Intent(mContext, ClosetActivity::class.java))
                }) {
                    Text(text = "옷장")
                }
                Button(onClick = {
                    mContext.startActivity(Intent(mContext, CalendarActivity::class.java))
                }) {
                    Text(text = "OOTD")
                }
            }
        }
    }
}