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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.time.LocalDate
import java.time.YearMonth
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
        val shoesItems = remember { mutableStateListOf<Uri>() }

        LaunchedEffect(Unit) {
            downloadImage("topimage") {
                topItems.add(it)
            }
            downloadImage("pantsimage") {
                pantsItems.add(it)
            }
            downloadImage("shoesimage") {
                shoesItems.add(it)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            CharacterBackground()
            CharacterFitClothes(topItems, pantsItems, shoesItems)
            CloseButton(Modifier.align(alignment = Alignment.BottomEnd))
        }
    }

    @Composable
    fun CharacterBackground() {
        Image(
            painter = painterResource(id = R.drawable.character), contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
    }

    @Composable
    fun CharacterFitClothes(topItems: List<Uri>, pantsItems: List<Uri>, shoesItems: List<Uri>) {
        Row {
            Divider(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(), color = Color.White
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(3f)
            ) {
                Spacer(modifier = Modifier.weight(6f))
                if (topItems.isNotEmpty()) {
                    ClothDragBox(Modifier.weight(12f), topItems)
                }
                if (pantsItems.isNotEmpty()) {
                    ClothDragBox(Modifier.weight(13f), pantsItems)
                }
                if (shoesItems.isNotEmpty()) {
                    ClothDragBox(Modifier.weight(2f), shoesItems)
                }
            }
            Divider(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(), color = Color.White
            )
        }
    }


    @Composable
    fun CloseButton(modifier: Modifier) {
        val mContext = LocalContext.current
        Button(
            onClick = {
                mContext.startActivity(Intent(mContext, ClosetActivity::class.java))
            },
            modifier = modifier
        ) {
            Text(text = "닫기")
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
        var clothIdx by remember { mutableIntStateOf(0) }
        Box(
            modifier = modifier
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
                                        clothIdx--
                                        clothIdx %= topItems.size
                                        if (clothIdx < 0) {
                                            clothIdx = topItems.size - 1
                                        }
                                        Toast
                                            .makeText(
                                                this@CharacterActivity,
                                                "총 ${topItems.size}개 : ${clothIdx + 1}",
                                                Toast.LENGTH_SHORT
                                            )
                                            .show()
                                    }
                                    offsetX = 0f
                                }

                                1 -> {
                                    if (offsetX < -300) {

                                        clothIdx++
                                        clothIdx %= topItems.size
                                        Toast
                                            .makeText(
                                                this@CharacterActivity,
                                                "총 ${topItems.size}개 : ${clothIdx + 1}",
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
                painter = rememberAsyncImagePainter(topItems[clothIdx]),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
        }
    }

}
