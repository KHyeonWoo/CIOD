package com.khw.ciod

import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.abs

class CalendarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Calendar()
        }
    }

    @Composable
    fun Calendar() {
        val currentDate = LocalDate.now()
        var year by remember { mutableIntStateOf(currentDate.year) }
        var month by remember { mutableIntStateOf(currentDate.monthValue) }
        val context = LocalContext.current
        val db by remember { mutableStateOf(AppDatabase.getDatabase(context)) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFDCE9EC))
        ) {
            CalendarHeader(year, month, onPreviousMonth = {
                val (newYear, newMonth) = updateMonth(year, month, -1)
                year = newYear
                month = newMonth
            }, onNextMonth = {
                val (newYear, newMonth) = updateMonth(year, month, 1)
                year = newYear
                month = newMonth
            })
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
                    .background(Color.White)
            ) {
                CalendarWeekHeader()

                var reLoad by remember { mutableIntStateOf(0) }
                CustomCalendarView(year, month) { day ->
                    CalendarOOTD(db, year, month, day, reLoad) { reLoad++ }
                }
            }
        }
    }

    private fun updateMonth(year: Int, month: Int, delta: Int): Pair<Int, Int> {
        var newYear = year
        var newMonth = month + delta
        if (newMonth < 1) {
            newMonth = 12
            newYear--
        } else if (newMonth > 12) {
            newMonth = 1
            newYear++
        }
        return Pair(newYear, newMonth)
    }

    @Composable
    private fun CalendarOOTD(
        db: AppDatabase, year: Int, month: Int, day: Int, reLoad: Int, reLoadEvent: () -> Unit
    ) {
        var ootd by remember { mutableStateOf<OOTD?>(null) }
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(year, month, day, reLoad) {
            coroutineScope.launch(Dispatchers.IO) {
                ootd = db.FavoriteClothesDao().getOOTD(
                    LocalDate.of(year, month, day).format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                )
            }
        }

        val currentDay = LocalDate.now().dayOfMonth
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp), contentAlignment = Alignment.Center
        ) {
            if (day == currentDay) {
                Text(
                    text = "$day",
                    fontSize = 16.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxHeight()
                        .width(32.dp)
                        .background(
                            Color(0xFF50B4B0), RoundedCornerShape(10.dp)
                        )
                )

            } else {
                Text(text = "$day", fontSize = 16.sp)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp), contentAlignment = Alignment.Center
        ) {
            var openDialog by remember { mutableStateOf(false) }

            ootd?.let {
                Column {

                    Image(
                        painter = rememberAsyncImagePainter(it.top),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(2f)
                            .clickable {
                                openDialog = true
                            },
                        contentScale = ContentScale.FillBounds
                    )
                    Image(
                        painter = rememberAsyncImagePainter(it.pants),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(2f)
                            .clickable {
                                openDialog = true
                            },
                        contentScale = ContentScale.FillBounds
                    )
                    Image(
                        painter = rememberAsyncImagePainter(it.shoes),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clickable {
                                openDialog = true
                            },
                        contentScale = ContentScale.FillBounds
                    )

                    if (openDialog) {
                        CharacterFit(db, it, year, month, day,
                            { openDialog = false },
                            { reLoadEvent() })
                    }
                }
            } ?: Column {
                Spacer(modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        openDialog = true
                    })

                if (openDialog) {
                    CharacterFit(db, null, year, month, day,
                        { openDialog = false },
                        { reLoadEvent() })
                }
            }
        }
    }

    @Composable
    private fun CustomCalendarView(
        year: Int, month: Int, content: @Composable (Int) -> Unit
    ) {
        Layout(modifier = Modifier
            .fillMaxSize()
            .drawWithCache {
                onDrawWithContent {
                    drawContent()
                }
            }, content = {
            val firstDayOfMonth = (LocalDate.of(year, month, 1).dayOfWeek.value % 7) * 2 - 1
            val daysInMonth = YearMonth.of(year, month).lengthOfMonth()
            val totalCells = firstDayOfMonth + daysInMonth

            (0..totalCells).forEach { index ->
                if (index > firstDayOfMonth) {
                    content(index - firstDayOfMonth)
                } else {
                    Spacer(modifier = Modifier)
                }
            }
        }) { measurables, constraints ->
            val dayWidth = constraints.maxWidth / 7
            val dayHeight = constraints.maxHeight / 6

            val placeables = measurables.mapIndexed { idx, measurable ->
                measurable.measure(
                    if (idx % 2 == 0) {
                        constraints.copy(
                            minWidth = dayWidth, maxWidth = dayWidth, minHeight = 40, maxHeight = 40
                        )
                    } else {
                        constraints.copy(
                            minWidth = dayWidth,
                            maxWidth = dayWidth,
                            minHeight = dayHeight - 40,
                            maxHeight = dayHeight - 40
                        )
                    }
                )
            }

            layout(constraints.maxWidth, constraints.maxHeight) {
                placeables.forEachIndexed { index, placeable ->
                    val dayIndex = index / 2
                    val row = dayIndex / 7
                    val column = dayIndex % 7

                    val xPosition = column * dayWidth
                    val yPosition = row * dayHeight

                    if (index % 2 == 0) {
                        placeable.place(x = xPosition, y = yPosition)
                    } else {
                        placeable.place(x = xPosition, y = yPosition + 40)
                    }
                }
            }
        }
    }

    @Composable
    fun CalendarHeader(
        year: Int, month: Int, onPreviousMonth: () -> Unit, onNextMonth: () -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Spacer(modifier = Modifier.weight(2f))
            Image(painter = painterResource(id = R.drawable.arrowbackicon),
                contentDescription = "",
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterVertically)
                    .clickable {
                        onPreviousMonth()
                    }

            )
            Spacer(modifier = Modifier.weight(1f))
            val currentMonth = YearMonth.of(year, month).month.toString()
            Text(
                text = "$currentMonth   $year", fontSize = 24.sp, fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.weight(1f))
            Image(painter = painterResource(id = R.drawable.arrowforwardicon),
                contentDescription = "",
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterVertically)
                    .clickable {
                        onNextMonth()
                    })
            Spacer(modifier = Modifier.weight(2f))
        }
    }

    @Composable
    fun CalendarWeekHeader() {
        Row(modifier = Modifier.fillMaxWidth()) {
            val daysOfWeek = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
            daysOfWeek.forEach { day ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9D9D9D)
                    )
                }
            }
        }
    }

    @Composable
    private fun CharacterFit(
        db: AppDatabase,
        ootd: OOTD?,
        year: Int,
        month: Int,
        day: Int,
        close: () -> Unit,
        reLoadEvent: () -> Unit
    ) {
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
        Dialog(onDismissRequest = { close() }) {

            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 24.dp,
                modifier = Modifier.padding(32.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {

                    val context = LocalContext.current
                    var topSavedItemIdx by remember { mutableIntStateOf(0) }
                    var pantsSavedItemIdx by remember { mutableIntStateOf(0) }
                    var shoesSavedItemIdx by remember { mutableIntStateOf(0) }

                    CharacterBackground()
                    CharacterFitClothes(ootd, topItems, pantsItems, shoesItems, {
                        topSavedItemIdx = it
                    }, {
                        pantsSavedItemIdx = it
                    }, {
                        shoesSavedItemIdx = it
                    })
                    Column(modifier = Modifier.align(alignment = Alignment.TopEnd)) {

                        val coroutineScope = rememberCoroutineScope()
                        if (ootd == null) {
                            Button(
                                onClick = {
                                    coroutineScope.launch(Dispatchers.IO) {
                                        db.FavoriteClothesDao().insertAll(
                                            OOTD(
                                                LocalDate.of(year, month, day)
                                                    .format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                                                topItems[topSavedItemIdx].toString(),
                                                pantsItems[pantsSavedItemIdx].toString(),
                                                shoesItems[shoesSavedItemIdx].toString()
                                            )
                                        )
                                    }
                                    Toast.makeText(context, "OOTD 등록 성공", Toast.LENGTH_SHORT).show()
                                    reLoadEvent()
                                    close()
                                }, modifier = Modifier
                            ) {
                                Text(text = "등록")
                            }
                        } else {
                            Button(onClick = {
                                coroutineScope.launch(Dispatchers.IO) {
                                    db.FavoriteClothesDao().delete(ootd)
                                }
                                Toast.makeText(context, "OOTD 삭제 성공", Toast.LENGTH_SHORT).show()
                                reLoadEvent()
                                close()
                            }) {
                                Text(text = "삭제")
                            }
                        }
                        Button(
                            onClick = {
                                close()
                            }, modifier = Modifier
                        ) {
                            Text(text = "취소")
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun CharacterBackground() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Image(
                painter = painterResource(id = R.drawable.character_face),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(4f)
            )

            Image(
                painter = painterResource(id = R.drawable.character_top),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(8f),
                contentScale = ContentScale.FillBounds
            )
            Image(
                painter = painterResource(id = R.drawable.character_pants),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(14f),
                contentScale = ContentScale.FillBounds
            )
            Image(
                painter = painterResource(id = R.drawable.character_shoes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(2f),
                contentScale = ContentScale.FillBounds
            )

        }
    }

    @Composable
    fun CharacterFitClothes(
        ootd: OOTD?,
        topItems: List<Uri>,
        pantsItems: List<Uri>,
        shoesItems: List<Uri>,
        getTopIdx: (Int) -> Unit,
        getPantsIdx: (Int) -> Unit,
        getShoesIdx: (Int) -> Unit
    ) {
        Row {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(3f)
            ) {
                Spacer(modifier = Modifier.weight(4f))
                if (topItems.isNotEmpty()) {
                    ClothDragBox(Modifier.weight(8f), ootd?.top, topItems) { getTopIdx(it) }
                }
                if (pantsItems.isNotEmpty()) {
                    ClothDragBox(Modifier.weight(14f), ootd?.pants, pantsItems) { getPantsIdx(it) }
                }
                if (shoesItems.isNotEmpty()) {
                    ClothDragBox(Modifier.weight(2f), ootd?.shoes, shoesItems) { getShoesIdx(it) }
                }
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
    fun ClothDragBox(
        modifier: Modifier, ootdCloth: String?, clothItems: List<Uri>, getIdx: (Int) -> Unit
    ) {
        var direction by remember { mutableIntStateOf(-1) }
        var offsetX by remember { mutableFloatStateOf(0f) }
        var clothIdx by remember { mutableIntStateOf(0) }
        Box(
            modifier = modifier.pointerInput(Unit) {
                detectDragGestures(onDrag = { change, dragAmount ->
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
                }, onDragEnd = {
                    when (direction) {
                        0 -> {
                            if (offsetX > 150) {
                                clothIdx--
                                if (clothIdx < 0) {
                                    clothIdx = clothItems.size - 1
                                }
                                getIdx(clothIdx)
                            }
                            offsetX = 0f
                        }

                        1 -> {
                            if (offsetX < -150) {
                                clothIdx++
                                clothIdx %= clothItems.size
                                getIdx(clothIdx)
                            }
                            offsetX = 0f
                        }
                    }
                })
            },
        ) {
            Divider(
                color = Color.White, modifier = Modifier.fillMaxSize()
            )

            Image(painter = ootdCloth?.let { rememberAsyncImagePainter(it) }
                ?: rememberAsyncImagePainter(clothItems[clothIdx]),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds)
        }
    }

    /**
     * Helper class for performing image segmentation using the SubjectSegmentation API.
     * This class encapsulates the functionality for obtaining foreground segmentation results from input images.
     */
}
