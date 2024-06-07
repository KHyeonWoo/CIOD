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
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.khw.ciod.ui.theme.CIODTheme
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

class ClosetActivity : ComponentActivity() {
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
        var successUpload by remember { mutableStateOf(false) }
        var clickedTopRef by remember { mutableStateOf<StorageReference?>(null) }
        var clickedTopUri by remember { mutableStateOf<String?>(null) }
        var clickedPantsRef by remember { mutableStateOf<StorageReference?>(null) }
        var clickedPantsUri by remember { mutableStateOf<String?>(null) }
        var clickedShoesRef by remember { mutableStateOf<StorageReference?>(null) }
        var clickedShoesUri by remember { mutableStateOf<String?>(null) }

        Row(modifier = Modifier.fillMaxSize()) {
            Closet(
                modifier = Modifier.weight(6f),
                successUpload,
                { successUpload = !successUpload },
                onTopImageClick = { clickedRef: StorageReference, clickedUri: String ->
                    clickedTopRef = clickedRef
                    clickedTopUri = clickedUri
                },
                onPantsImageClick = { clickedRef: StorageReference, clickedUri: String ->
                    clickedPantsRef = clickedRef
                    clickedPantsUri = clickedUri
                },
                onShoesImageClick = { clickedRef: StorageReference, clickedUri: String ->
                    clickedShoesRef = clickedRef
                    clickedShoesUri = clickedUri
                }
            )
            Divider(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
            )
            var isPopup by remember { mutableStateOf(false) }
            Character(Modifier.weight(4f), clickedTopUri, clickedPantsUri, clickedShoesUri,
                {
                    isPopup = true
                },
                {
                    isPopup = true
                    clickedPantsUri = null
                },
                {
                    isPopup = true
                    clickedShoesUri = null
                })

            clickedTopRef?.let { clickedRef ->
                ImagePopup(isPopup, clickedTopUri, clickedRef,
                    {
                        clickedTopRef = null
                        clickedTopUri = null
                    },
                    {
                        isPopup = false
                    },
                    {
                        successUpload = !successUpload
                    }
                )
            }
        }
    }


    @Composable
    fun Closet(
        modifier: Modifier,
        successUpload: Boolean,
        successUploadEvent: () -> Unit,
        onTopImageClick: (StorageReference, String) -> Unit,
        onPantsImageClick: (StorageReference, String) -> Unit,
        onShoesImageClick: (StorageReference, String) -> Unit
    ) {

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
                    successUploadEvent()
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
                    successUploadEvent()
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
                    successUploadEvent()
                },
                onImageClick = onShoesImageClick
            )
        }
    }


    private @Composable
    fun GlideImageView(item: String, modifier: Modifier, imageOnClick: () -> Unit) {

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
                contentDescription = "Image",
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        imageOnClick()
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
        onImageClick: (StorageReference, String) -> Unit
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
            ImageUploadPopup(
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

    private fun imageDelete(
        context: Context,
        desertRef: StorageReference
    ) {
        desertRef.delete().addOnSuccessListener {
            Toast.makeText(context, "$desertRef 사진 삭제 성공", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(context, "사진 삭제 실패", Toast.LENGTH_SHORT).show()
        }
    }

    @Composable
    fun ImageUploadPopup(showDialog: Boolean, upLoad: () -> Unit, cancel: () -> Unit, uri: Uri) {
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
        onImageClick: (StorageReference, String) -> Unit,
        returnIdx: (Int) -> Unit
    ) {
        val storageRef = Firebase.storage.reference.child(category)
        val itemsRef = remember { mutableStateListOf<StorageReference>() }
        val itemsUri = remember { mutableStateListOf<String>() }
        val idxList = remember { mutableListOf(-1) }
        var num by remember { mutableIntStateOf(0) }

        LaunchedEffect(successUpload) {
            itemsRef.clear()
            itemsUri.clear()

            val listResult = storageRef.listAll().await()

            coroutineScope {
                val downloadTasks = listResult.items.map { clothRef ->
                    async {
                        try {
                            val uri = clothRef.downloadUrl.await().toString()
                            itemsRef.add(clothRef)
                            itemsUri.add(uri)
                            for (char in clothRef.name) {
                                if (char in '0'..'9') {
                                    num = num * 10 + char.toString().toInt()
                                }
                            }
                            idxList.add(num)
                            num = 0
                        } catch (e: Exception) {
                            // Handle exceptions if needed
                        }
                    }
                }
                downloadTasks.forEach { it.await() }
            }

            returnIdx(idxList.max())
        }
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(top = 4.dp, start = 2.dp)
        ) {
            (itemsRef zip itemsUri).chunked(3).forEach { item ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    item.forEach {
                        Column {
                            GlideImage(
                                imageModel = it.second,
                                contentDescription = "Image",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clickable {
                                        onImageClick(it.first, it.second)
                                    }
                            )
                        }
                    }
                }
            }

            if (itemsUri.isEmpty()) {
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
            val mContext = LocalContext.current
            Image(
                painter = painterResource(id = R.drawable.character), contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        mContext.startActivity(Intent(mContext, CharacterActivity::class.java))
                    },
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
            Button(
                onClick = { finish() },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Text(text = "X")
            }
        }

    }

    private @Composable
    fun ImagePopup(
        showDialog: Boolean,
        item: String?,
        clickedTopRef: StorageReference,
        takeOff: () -> Unit,
        cancel: () -> Unit,
        delete: () -> Unit
    ) {
        if (showDialog) {
            Dialog(onDismissRequest = { }) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 24.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (item != null) {
                            GlideImage(
                                imageModel = item,
                                contentDescription = "Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(660.dp),
                                contentScale = ContentScale.FillBounds
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            val context = LocalContext.current
                            Button(onClick = {

                            }) {
                                Text(text = "❤️")
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Button(onClick = {
                                takeOff()
                                cancel()
                            }) {
                                Text(text = "벗기기")
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Button(onClick = {
                                imageDelete(context, clickedTopRef)
                                delete()
                                takeOff()
                                cancel()
                            }) {
                                Text(text = "삭제")
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Button(onClick = {
                                cancel()
                            }) {
                                Text(text = "취소")
                            }
                        }
                    }
                }
            }
        }
    }
}