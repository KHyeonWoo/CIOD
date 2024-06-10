package com.khw.ciod

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import com.khw.ciod.ui.theme.CIODTheme
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDate
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

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

        Column(modifier = Modifier.fillMaxSize()) {

            var isTopPopup by remember { mutableStateOf(false) }
            var isPantsPopup by remember { mutableStateOf(false) }
            var isShoesPopup by remember { mutableStateOf(false) }
            Character(Modifier.weight(7f), clickedTopUri, clickedPantsUri, clickedShoesUri,
                {
                    isTopPopup = true
                },
                {
                    isPantsPopup = true
                },
                {
                    isShoesPopup = true
                })

            clickedTopRef?.let { clickedRef ->
                ImagePopup(isTopPopup, clickedTopUri, clickedRef,
                    {
                        clickedTopRef = null
                        clickedTopUri = null
                    },
                    {
                        isTopPopup = false
                    },
                    {
                        successUpload = !successUpload
                    }
                )
            }

            clickedPantsRef?.let { clickedRef ->
                ImagePopup(isPantsPopup, clickedPantsUri, clickedRef,
                    {
                        clickedPantsRef = null
                        clickedPantsUri = null
                    },
                    {
                        isPantsPopup = false
                    },
                    {
                        successUpload = !successUpload
                    }
                )
            }

            clickedShoesRef?.let { clickedRef ->
                ImagePopup(isShoesPopup, clickedShoesUri, clickedRef,
                    {
                        clickedShoesRef = null
                        clickedShoesUri = null
                    },
                    {
                        isShoesPopup = false
                    },
                    {
                        successUpload = !successUpload
                    }
                )
            }
            CategoryTapLayout(
                Modifier.weight(3f), successUpload, { successUpload = !successUpload },
                { clickedRef: StorageReference, clickedUri: String ->
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
        }
    }

    @OptIn(ExperimentalPagerApi::class)
    @Composable
    fun CategoryTapLayout(
        modifier: Modifier,
        successUpload: Boolean,
        successUploadEvent: () -> Unit,
        onTopImageClick: (StorageReference, String) -> Unit,
        onPantsImageClick: (StorageReference, String) -> Unit,
        onShoesImageClick: (StorageReference, String) -> Unit
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {

                val pages = listOf(
                    R.drawable.category_top,
                    R.drawable.category_pants,
                    R.drawable.category_shoes
                )
                val pagerState = rememberPagerState()
                val coroutineScope = rememberCoroutineScope()

                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
                        )
                    },
                    backgroundColor = Color.Black,
                    contentColor = Color.White
                ) {
                    pages.forEachIndexed { index, title ->
                        Tab(
                            text = {
                                Image(
                                    painter = painterResource(id = title),
                                    contentDescription = "title",
                                    modifier = Modifier.size(40.dp)
                                )
                            },
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.scrollToPage(index)
                                }
                            }
                        )
                    }
                }

                HorizontalPager(
                    count = pages.size,
                    state = pagerState,
                ) { page ->
                    if (page.toString() == "0") {
                        ImageSection(
                            category = "topimage",
                            successUpload = successUpload,
                            onUpload = {
                                successUploadEvent()
                            },
                            onImageClick = onTopImageClick
                        )
                    } else if (page.toString() == "1") {
                        ImageSection(
                            category = "pantsimage",
                            successUpload = successUpload,
                            onUpload = {
                                successUploadEvent()
                            },
                            onImageClick = onPantsImageClick
                        )
                    } else if (page.toString() == "2") {
                        ImageSection(
                            category = "shoesimage",
                            successUpload = successUpload,
                            onUpload = {
                                successUploadEvent()
                            },
                            onImageClick = onShoesImageClick
                        )
                    }

                }
            }
        }

    }

    private @Composable
    fun GlideImageView(item: String, modifier: Modifier, imageOnClick: () -> Unit) {

        Row(
            modifier = modifier
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.weight(1f))
            GlideImage(
                imageModel = item,
                contentDescription = "Image",
                modifier = Modifier
                    .fillMaxSize()
                    .weight(4f)
                    .clickable {
                        imageOnClick()
                    },
                contentScale = ContentScale.FillBounds
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }

    @Composable
    fun ImageSection(
        category: String,
        successUpload: Boolean,
        onUpload: () -> Unit,
        onImageClick: (StorageReference, String) -> Unit
    ) {
        var idx by remember {
            mutableIntStateOf(-1)
        }
        Box(modifier = Modifier.fillMaxSize()) {
            ImageGrid(category, successUpload, onImageClick) { idx = it }
            GalleryUploadButton(
                Modifier.align(
                    Alignment.BottomEnd
                ), idx + 1, category, onUpload
            )
        }
    }

    @Composable
    fun GalleryUploadButton(
        modifier: Modifier,
        index: Int,
        category: String,
        onUpload: () -> Unit
    ) {

        val context = LocalContext.current

        // State to hold the output bitmap after segmentation
        val outputImage: MutableState<Bitmap?> = remember {
            mutableStateOf<Bitmap?>(null)
        }
        val inputImage: MutableState<Bitmap?> = remember {
            mutableStateOf(null)
        }

        val imageCropLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
            if (result.isSuccessful) {
                inputImage.value =
                    MediaStore.Images.Media.getBitmap(context.contentResolver, result.uriContent)
            }
            else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

        Image(painter = painterResource(id = R.drawable.addicon),
            contentDescription = "add",
            modifier = modifier
                .size(72.dp)
                .padding(end = 16.dp)
                .clickable {
                    val cropOption = CropImageContractOptions(CropImage.CancelledResult.uriContent, CropImageOptions())
                    imageCropLauncher.launch(cropOption)
                })

        // State to track the loading status during image segmentation
        var loading: Boolean by remember {
            mutableStateOf(false)
        }

        // Effect to trigger image segmentation when the input image changes
        LaunchedEffect(key1 = inputImage.value) {
            inputImage.value?.let { bitmap ->
                // Set loading to true before starting the segmentation process
                loading = true
                // Perform image segmentation using ImageSegmentationHelper
                val output = ImageSegmentationHelper.getResult(bitmap)
                // Update the outputImage state with the segmented result
                outputImage.value = output
                // Set loading back to false after segmentation is complete
                loading = false
            }
        }

        outputImage.value?.let { bitmap ->
            var showDialog by remember { mutableStateOf(true) }
            ImageUploadPopup(
                showDialog = showDialog,
                upLoad = {
                    imageUpload(context, category, index, bitmap, onUpload)
                },
                cancel = {
                    showDialog = false
                    outputImage.value = null
                },
                bitmap = bitmap
            )
        }
    }

    // Bitmap을 파일로 저장하고 Uri를 반환하는 함수
    fun saveBitmapToFile(context: Context, bitmap: Bitmap, fileName: String): Uri? {
        val file = File(context.cacheDir, fileName)
        return try {
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
            Uri.fromFile(file)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun imageUpload(
        context: Context,
        category: String,
        index: Int,
        bitmap: Bitmap,
        onUpload: () -> Unit
    ) {
        val fileName = "$category$index.png"
        val imageUri = saveBitmapToFile(context, bitmap, fileName)

        imageUri?.let {
            val storageRef = Firebase.storage.getReference(category)
            val mountainsRef = storageRef.child(fileName)

            val uploadTask = mountainsRef.putFile(it)
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
    fun ImageUploadPopup(
        showDialog: Boolean,
        upLoad: () -> Unit,
        cancel: () -> Unit,
        bitmap: Bitmap
    ) {
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
                                painter = rememberAsyncImagePainter(bitmap),
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
            (itemsRef zip itemsUri).chunked(5).forEach { item ->
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
                                    },
                                contentScale = ContentScale.FillBounds
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
            Image(
                painter = painterResource(id = R.drawable.closetbackground),
                contentDescription = "background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
            Divider(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(80.dp),
                color = Color(0xFFEDF3F5)
            )
            Image(
                painter = painterResource(id = R.drawable.charactershadow),
                contentDescription = null,
                modifier = Modifier
                    .size(320.dp, 56.dp)
                    .align(Alignment.BottomCenter),
                contentScale = ContentScale.FillBounds
            )
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(4f)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.character_face),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(4f)
                    )
                    //상의
                    clickedTop?.let { topUri ->
                        GlideImageView(
                            topUri, modifier = Modifier.weight(8f)
                        ) { topOnClick() }
                    } ?: Image(
                        painter = painterResource(id = R.drawable.character_top),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(8f),
                        contentScale = ContentScale.FillBounds
                    )

                    //하의
                    clickedPants?.let { pantsUri ->
                        GlideImageView(
                            pantsUri, modifier = Modifier.weight(14f)
                        ) { pantsOnClick() }
                    } ?: Image(
                        painter = painterResource(id = R.drawable.character_pants),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(14f),
                        contentScale = ContentScale.FillBounds
                    )


                    //신발
                    clickedShoes?.let { shoesUri ->
                        GlideImageView(
                            shoesUri, modifier = Modifier.weight(2f)
                        ) { shoesOnClick() }
                    } ?: Image(
                        painter = painterResource(id = R.drawable.character_shoes),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(2f),
                        contentScale = ContentScale.FillBounds
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }
            val mContext = LocalContext.current
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)

            ) {
                Image(painter = painterResource(id = R.drawable.calendaricon),
                    contentDescription = "calendar",
                    modifier = Modifier
                        .size(48.dp)
                        .clickable {
                            mContext.startActivity(Intent(mContext, CalendarActivity::class.java))
                        })
                Text(
                    text = LocalDate.now().month.toString()
                        .substring(0, 3) + "  " + LocalDate.now().dayOfMonth.toString(),
                    fontSize = 12.sp, textAlign = TextAlign.Center,
                    modifier = Modifier.padding(start = 8.dp)
                )
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
                                    .height(240.dp),
                                contentScale = ContentScale.FillBounds
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            val context = LocalContext.current
                            Spacer(modifier = Modifier.weight(1f))
                            Button(onClick = {
                                takeOff()
                                cancel()
                            }) {
                                Text(text = "Off")
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

    /**
     * Helper class for performing image segmentation using the SubjectSegmentation API.
     * This class encapsulates the functionality for obtaining foreground segmentation results from input images.
     */
    object ImageSegmentationHelper {

        // Options for configuring the SubjectSegmenter
        private val options = SubjectSegmenterOptions.Builder()
            .enableForegroundConfidenceMask()
            .enableForegroundBitmap()
            .build()

        // SubjectSegmenter instance initialized with the specified options
        private val segmenter = SubjectSegmentation.getClient(options)

        /**
         * Asynchronously processes the given input Bitmap image and retrieves the foreground segmentation result.
         *
         * @param image The input image in Bitmap format to be segmented.
         * @return A suspend function that, when invoked, provides the result Bitmap of the foreground segmentation.
         * @throws Exception if there is an error during the segmentation process.
         */
        suspend fun getResult(image: Bitmap) = suspendCoroutine {
            // Convert the input Bitmap image to InputImage format
            val inputImage = InputImage.fromBitmap(image, 0)

            // Process the input image using the SubjectSegmenter
            segmenter.process(inputImage)
                .addOnSuccessListener { result ->
                    // Resume the coroutine with the foreground Bitmap result on success
                    it.resume(result.foregroundBitmap)
                }
                .addOnFailureListener { e ->
                    // Resume the coroutine with an exception in case of failure
                    it.resumeWithException(e)
                }
        }
    }

    /**
     * Composable for the ImageSegmenterScreen, providing an interactive screen for image segmentation.
     * Utilizes Jetpack Compose for UI components and integrates with the ImageSegmentationHelper for segmentation processing.
     */
    @Composable
    fun ImageSegmenterScreen() {
        val context = LocalContext.current

        // State to hold the output bitmap after segmentation
        val outputImage: MutableState<Bitmap?> = remember {
            mutableStateOf<Bitmap?>(null)
        }

        // State to hold the input bitmap before segmentation
        val inputImage: MutableState<Bitmap?> = remember {
            mutableStateOf(null)
        }

        // ActivityResultLauncher for picking visual media from the device
        val pickMedia = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
            // Callback for handling the result of media selection
            onResult = { uri ->
                if (uri != null) {
                    // Load the selected image into the inputImage state
                    inputImage.value =
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                } else {
                    Log.d("PhotoPicker", "No media selected")
                }
            })

        // State to track the loading status during image segmentation
        var loading: Boolean by remember {
            mutableStateOf(false)
        }

        // State to toggle between displaying the segmented result and the original image
        var isOriginal: Boolean by remember {
            mutableStateOf(false)
        }

        // Effect to trigger image segmentation when the input image changes
        LaunchedEffect(key1 = inputImage.value) {
            inputImage.value?.let { bitmap ->
                // Set loading to true before starting the segmentation process
                loading = true
                // Perform image segmentation using ImageSegmentationHelper
                val output = ImageSegmentationHelper.getResult(bitmap)
                // Update the outputImage state with the segmented result
                outputImage.value = output
                // Set loading back to false after segmentation is complete
                loading = false
            }
        }

        // Scaffold composable for overall screen structure
        Scaffold { paddingValues ->
            Box(modifier = Modifier.background(Color.White)) {
                // Row containing the "Open Gallery" button
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxWidth()
                ) {
                    Button(onClick = {
                        // Launch the media picker to select an image from the gallery
                        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) {
                        Text(text = "Open Gallery")
                    }
                }
                // Box containing the image display area and loading indicator
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    // Display the segmented result or original image based on the isOriginal state
                    if (outputImage.value != null && inputImage.value != null) {
                        Image(
                            bitmap = if (!isOriginal) outputImage.value!!.asImageBitmap() else inputImage.value!!.asImageBitmap(),
                            contentDescription = "",
                            Modifier
                                .fillMaxWidth()
                                // Toggle isOriginal state on image click for comparison
                                .clickable {
                                    isOriginal = !isOriginal
                                }
                        )
                    }

                    // Display a loading indicator while image segmentation is in progress
                    if (loading) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

}
