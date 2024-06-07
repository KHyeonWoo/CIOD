package com.khw.ciod

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.khw.ciod.ui.theme.CIODTheme
import com.skydoves.landscapist.glide.GlideImage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContent {
            CIODTheme {
                MainScreen()
            }
        }
    }

    @Composable
    fun MainScreen() {
        var clickedTop by remember { mutableStateOf<String?>(null) }
        var clickedPants by remember { mutableStateOf<String?>(null) }
        var clickedShoes by remember { mutableStateOf<String?>(null) }

        Row(modifier = Modifier.fillMaxSize()) {
            Closet(
                modifier = Modifier.weight(6f),
                onTopImageClick = { clickedTop = it },
                onPantsImageClick = { clickedPants = it },
                onShoesImageClick = { clickedShoes = it }
            )
            Divider(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
            )
            Character(Modifier.weight(4f), clickedTop, clickedPants, clickedShoes,
                {
                    clickedTop = null
                },
                {
                    clickedPants = null
                },
                {
                    clickedShoes = null
                })
        }
    }

    @Composable
    fun Closet(
        modifier: Modifier,
        onTopImageClick: (String) -> Unit,
        onPantsImageClick: (String) -> Unit,
        onShoesImageClick: (String) -> Unit
    ) {
        var successUpload by remember { mutableStateOf(false) }

        Column(
            modifier = modifier
                .fillMaxHeight()
        ) {
            ImageSection(
                modifier = Modifier.weight(4f),
                label = "상의",
                category = "topimage",
                successUpload = successUpload,
                onUpload = {
                    successUpload = !successUpload
                },
                onImageClick = onTopImageClick
            )
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
            )
            ImageSection(
                modifier = Modifier.weight(4f),
                label = "하의",
                category = "pantsimage",
                successUpload = successUpload,
                onUpload = {
                    successUpload = !successUpload
                },
                onImageClick = onPantsImageClick
            )
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
            )
            ImageSection(
                modifier = Modifier.weight(2f),
                label = "신발",
                category = "shoesimage",
                successUpload = successUpload,
                onUpload = {
                    successUpload = !successUpload
                },
                onImageClick = onShoesImageClick
            )
        }
    }


    private @Composable
    fun GlideImageView(item: String, modifier: Modifier, topOnClick: () -> Unit) {

        Box(
            modifier = modifier
                .fillMaxSize()
        ) {
            Divider(
                modifier = Modifier.fillMaxSize(),
                color = Color.White
            )
            GlideImage(
                imageModel = item,
                contentDescription = "Top Image",
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        topOnClick()
                    },
                contentScale = ContentScale.FillBounds
            )
        }
    }

    @Composable
    fun ImageSection(
        modifier: Modifier = Modifier,
        label: String,
        category: String,
        successUpload: Boolean,
        onUpload: () -> Unit,
        onImageClick: (String) -> Unit
    ) {
        var idx by remember {
            mutableIntStateOf(-1)
        }
        Column(modifier = modifier) {
            Row {
                Text(
                    text = label, modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(start = 8.dp),
                    style = TextStyle(
                        color = Color.Blue,
                        fontSize = 24.sp,
                        FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                GalleryUploadButton(idx + 1, category, onUpload)
            }
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
            )
            ImageGrid(category, successUpload, onImageClick) { idx = it }
        }
    }

    @Composable
    fun GalleryUploadButton(index: Int, category: String, onUpload: () -> Unit) {
        var imageUri by remember { mutableStateOf<Uri?>(null) }
        val launcher =
            rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                imageUri = uri
            }

        Button(onClick = { launcher.launch("image/*") }) {
            Text(text = "추가")
        }

        imageUri?.let { uri ->
            val context = LocalContext.current
            var showDialog by remember { mutableStateOf(true) }
            ImagePopup(
                showDialog = showDialog,
                upLoad = {
                    imageUpload(context, category, index, uri, onUpload)
                },
                cancel = {
                    showDialog = false
                    imageUri = null
                },
                uri = uri
            )
        }
    }

    private fun imageUpload(
        context: Context,
        category: String,
        index: Int,
        uri: Uri,
        onUpload: () -> Unit
    ) {
        val storageRef = Firebase.storage.getReference(category)
        val fileName = "$category$index"
        val mountainsRef = storageRef.child("$fileName.png")

        val uploadTask = mountainsRef.putFile(uri)
        uploadTask.addOnSuccessListener {
            Toast.makeText(context, "사진 업로드 성공", Toast.LENGTH_SHORT).show()
            onUpload()
        }.addOnProgressListener {
            Toast.makeText(context, "사진 업로드 중", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(context, "사진 업로드 실패", Toast.LENGTH_SHORT).show()
        }
    }

    @Composable
    fun ImagePopup(showDialog: Boolean, upLoad: () -> Unit, cancel: () -> Unit, uri: Uri) {
        if (showDialog) {
            Dialog(onDismissRequest = { }) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 24.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(400.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(onClick = {
                                upLoad()
                                cancel()
                            }) {
                                Text(text = "등록")
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            TextButton(onClick = { cancel() }) {
                                Text(text = "취소")
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ImageGrid(
        category: String,
        successUpload: Boolean,
        onImageClick: (String) -> Unit,
        returnIdx: (Int) -> Unit
    ) {
        val storageRef = Firebase.storage.reference.child(category)
        val items = remember { mutableStateListOf<String>() }
        val idxList = remember { mutableListOf(-1) }
        var num by remember { mutableStateOf(0) }
        LaunchedEffect(successUpload) {
            items.clear()
            storageRef.listAll().addOnSuccessListener {
                it.items.forEach { clothRef ->
                    clothRef.downloadUrl.addOnSuccessListener { uri ->
                        items.add(uri.toString())
                    }
                    for (char in clothRef.name) {
                        if (char in '0'..'9') {
                            num = num * 10 + char.toString().toInt()
                        }
                    }
                    idxList.add(num)
                }
            }
        }
        returnIdx(idxList.max())
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(top = 4.dp, start = 2.dp)
        ) {
            items.chunked(3).forEach { rowItems ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    rowItems.forEach { item ->
                        Column {
                            GlideImage(
                                imageModel = item,
                                contentDescription = "Image",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clickable {
                                        onImageClick(item)
                                    }
                            )
                        }
                    }
                }
            }

            if (items.isEmpty()) {
                Text(text = "Loading image...", modifier = Modifier.padding(16.dp))
            }
        }
    }

    @Composable
    private fun Character(
        modifier: Modifier,
        clickedTop: String?,
        clickedPants: String?,
        clickedShoes: String?,
        topOnClick: () -> Unit,
        pantsOnClick: () -> Unit,
        shoesOnClick: () -> Unit
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.character), contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.weight(5f))

                //상의
                clickedTop?.let { topUri ->
                    GlideImageView(
                        topUri, modifier = Modifier.weight(10f)
                    ) { topOnClick() }
                } ?: Spacer(modifier = Modifier.weight(10f))

                //하의
                clickedPants?.let { pantsUri ->
                    GlideImageView(
                        pantsUri, modifier = Modifier.weight(13f)
                    ) { pantsOnClick() }
                } ?: Spacer(modifier = Modifier.weight(13f))

                //신발
                clickedShoes?.let { shoesUri ->
                    GlideImageView(
                        shoesUri, modifier = Modifier.weight(2f)
                    ) { shoesOnClick() }
                } ?: Spacer(modifier = Modifier.weight(2f))
            }

            val mContext = LocalContext.current

            Button(
                onClick = {
                    mContext.startActivity(Intent(mContext, CharacterActivity::class.java))
                },
                modifier = Modifier.align(alignment = Alignment.TopEnd)
            ) {
                Text("드레스룸")
            }

        }

    }

}
