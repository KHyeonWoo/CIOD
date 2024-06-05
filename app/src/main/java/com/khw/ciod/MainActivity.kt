package com.khw.ciod

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.khw.ciod.ui.theme.CIODTheme
import com.skydoves.landscapist.glide.GlideImage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CIODTheme {
                MainScreen()
            }
        }
    }


    @Composable
    fun MainScreen() {
        var topIndex by remember { mutableIntStateOf(0) }
        var pantsIndex by remember { mutableIntStateOf(0) }
        var successUpload by remember { mutableStateOf(false) }

        Row(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier
                .fillMaxHeight()
                .weight(6f)) {
                Column(modifier = Modifier.weight(5f)) {
                    ImageSection("상의", topIndex, "topimage", successUpload, onUpload = {
                        topIndex++
                        successUpload = !successUpload
                    })
                }
                Column(modifier = Modifier.weight(5f)) {
                    ImageSection("하의", pantsIndex, "pantsimage", successUpload, onUpload = {
                        pantsIndex++
                        successUpload = !successUpload
                    })
                }
            }

            Spacer(modifier = Modifier.weight(4f))
        }
    }

    @Composable
    fun ImageSection(
        label: String,
        index: Int,
        category: String,
        successUpload: Boolean,
        onUpload: () -> Unit
    ) {
        Column {
            Row {
                Text(text = label)
                GalleryUploadButton(index, category, onUpload)
            }
            ImageGrid(category, successUpload)
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
            Text(text = "갤러리")
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

    @Composable
    fun ImageGrid(category: String, successUpload: Boolean) {
        val storageRef = Firebase.storage.reference.child(category)
        val items = remember { mutableStateListOf<String>() }

        LaunchedEffect(successUpload) {
            items.clear()
            storageRef.listAll().addOnSuccessListener {
                for (item in it.items) {
                    item.downloadUrl.addOnSuccessListener { uri ->
                        items.add(uri.toString())
                    }
                }
            }
        }

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            items.chunked(3).forEach { rowItems ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    rowItems.forEach { item ->
                        Column {
                            GlideImage(
                                imageModel = item,
                                contentDescription = "Image",
                                modifier = Modifier.size(80.dp)
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
    fun ImagePopup(showDialog: Boolean, upLoad: () -> Unit, cancel: () -> Unit, uri: Uri) {
        if (showDialog) {
            Dialog(onDismissRequest = { }) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 24.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp)
                        )
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
}
