package com.khw.ciod

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun BackgroundScreen() {
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
    }

}