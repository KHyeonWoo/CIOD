package com.khw.ciod

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlin.math.abs

class CharacterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Character()
        }
    }

    @Composable
    private fun Character() {
        val topItems = remember { mutableStateListOf<Uri>() }
        val pantsItems = remember { mutableStateListOf<Uri>() }

        LaunchedEffect(Unit) {
            downloadImage("topimage") {
                topItems.add(it)
            }
            downloadImage("pantsimage") {
                pantsItems.add(it)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.character), contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.weight(6f))
                if (topItems.isNotEmpty()) {
                    ClothDragBox(Modifier.weight(12f), topItems)
                }
                if (pantsItems.isNotEmpty()) {
                    ClothDragBox(Modifier.weight(13f), pantsItems)
                }
                Spacer(modifier = Modifier.weight(2f))
            }

            val mContext = LocalContext.current
            Button(
                onClick = {
                    mContext.startActivity(Intent(mContext, MainActivity::class.java))
                },
                modifier = Modifier.align(alignment = Alignment.BottomEnd)
            ) {
                Text("닫기")
            }
        }
    }

    private fun downloadImage(clothRef: String, getItem: (Uri) -> Unit) {
        val storageRef = Firebase.storage.reference.child(clothRef)

        storageRef.listAll().addOnSuccessListener {
            it.items.forEach { itemRef ->
                itemRef.downloadUrl.addOnSuccessListener { uri ->
                    getItem(uri)
                }
            }
        }
    }

    @Composable
    fun ClothDragBox(modifier: Modifier, topItems: List<Uri>) {
        var direction by remember { mutableIntStateOf(-1) }
        var offsetX by remember { mutableFloatStateOf(0f) }
        var topIdx by remember { mutableIntStateOf(0) }
        Box(
            modifier = modifier
                .fillMaxWidth()
                .pointerInput(Unit) {

                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val (x, y) = dragAmount
                            if (abs(x) > abs(y)) {
                                when {
                                    x > 0 -> {    //right
                                        offsetX += x
                                        Log.d(ContentValues.TAG, "offsetX($offsetX)")
                                        direction = 0
                                    }

                                    x < 0 -> {  // left
                                        offsetX += x
                                        Log.d(ContentValues.TAG, "offsetX($offsetX)")
                                        direction = 1
                                    }
                                }
                            }
                        },
                        onDragEnd = {
                            when (direction) {
                                0 -> {
                                    if (offsetX > 300) {
                                        topIdx--
                                        topIdx %= topItems.size
                                        if (topIdx < 0) {
                                            topIdx = topItems.size - 1
                                        }
                                        Toast
                                            .makeText(
                                                this@CharacterActivity,
                                                "총 ${topItems.size}개 : ${topIdx + 1}",
                                                Toast.LENGTH_SHORT
                                            )
                                            .show()
                                    }
                                    offsetX = 0f
                                }

                                1 -> {
                                    if (offsetX < -300) {

                                        topIdx++
                                        topIdx %= topItems.size
                                        Toast
                                            .makeText(
                                                this@CharacterActivity,
                                                "총 ${topItems.size}개 : ${topIdx + 1}",
                                                Toast.LENGTH_SHORT
                                            )
                                            .show()
                                    }
                                    offsetX = 0f
                                }
                            }
                        }
                    )
                },
        ) {
            Divider(
                color = Color.White,
                modifier = Modifier.fillMaxSize()
            )

            Image(
                painter = rememberAsyncImagePainter(topItems[topIdx]),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }

    }
}
